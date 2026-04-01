package org.pakicek.monoforecast.data.ble

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.content.ComponentName
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.pakicek.monoforecast.data.features.BackgroundFeature
import org.pakicek.monoforecast.domain.model.BleState
import org.pakicek.monoforecast.domain.model.ble.WheelDevice
import org.pakicek.monoforecast.domain.model.ble.WheelMetrics

class BleFeature(private val context: Context) : BackgroundFeature<BleState> {

    companion object {
        private const val TAG = "BleFeature"
    }

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private val _state = MutableStateFlow<BleState>(BleState.DISCONNECTED)
    override val state: StateFlow<BleState> = _state.asStateFlow()

    private val _deviceFound = MutableSharedFlow<WheelDevice>()
    val deviceFound = _deviceFound.asSharedFlow()

    private val _connectionState = MutableSharedFlow<Boolean>()
    val connectionState = _connectionState.asSharedFlow()

    private val _metricsUpdate = MutableSharedFlow<WheelMetrics>()
    val metricsUpdate = _metricsUpdate.asSharedFlow()

    private val _error = MutableSharedFlow<String>()
    val error = _error.asSharedFlow()

    private var bleService: BleService? = null
    private var isServiceBound = false
    private var broadcastReceiver: BroadcastReceiver? = null
    private var pendingDevice: WheelDevice? = null

    @RequiresPermission(value = "android.permission.BLUETOOTH_CONNECT")
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            Log.d(TAG, "BleService connected")
            val binder = service as BleService.LocalBinder
            bleService = binder.getService()
            isServiceBound = true

            setupBroadcastReceiver()

            pendingDevice?.let { device ->
                Log.d(TAG, "Executing pending connection to: ${device.name}")
                connectToDevice(device)
                pendingDevice = null
            }

            if (bleService?.isConnected() == true) {
                _state.value = BleState.CONNECTED
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            Log.d(TAG, "BleService disconnected")
            bleService = null
            isServiceBound = false
            _state.value = BleState.DISCONNECTED
        }
    }

    init {
        bindToBleService()
    }

    private fun bindToBleService() {
        Log.d(TAG, "Binding to BleService")
        val intent = Intent(context, BleService::class.java)
        context.startService(intent)
        context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    private fun setupBroadcastReceiver() {
        Log.d(TAG, "Setting up broadcast receiver")

        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                when (intent?.action) {
                    BleService.ACTION_DEVICE_FOUND -> {
                        val device = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            intent.getParcelableExtra(BleService.EXTRA_DEVICE, WheelDevice::class.java)
                        } else {
                            @Suppress("DEPRECATION")
                            intent.getParcelableExtra(BleService.EXTRA_DEVICE)
                        }
                        device?.let {
                            Log.d(TAG, "Device found: ${it.name}")
                            scope.launch {
                                _deviceFound.emit(it)
                            }
                        }
                    }

                    BleService.ACTION_METRICS_UPDATE -> {
                        val metrics = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            intent.getParcelableExtra(BleService.EXTRA_METRICS, WheelMetrics::class.java)
                        } else {
                            @Suppress("DEPRECATION")
                            intent.getParcelableExtra(BleService.EXTRA_METRICS)
                        }
                        metrics?.let {
                            Log.d(TAG, "Metrics update received")
                            scope.launch {
                                _metricsUpdate.emit(it)
                            }
                        }
                    }

                    BleService.ACTION_CONNECTED -> {
                        Log.d(TAG, "BLE connected")
                        _state.value = BleState.CONNECTED
                        scope.launch {
                            _connectionState.emit(true)
                        }
                    }

                    BleService.ACTION_DISCONNECTED -> {
                        Log.d(TAG, "BLE disconnected")
                        _state.value = BleState.DISCONNECTED
                        scope.launch {
                            _connectionState.emit(false)
                        }
                    }

                    BleService.ACTION_SCAN_STARTED -> {
                        Log.d(TAG, "Ble scan started")
                        _state.value = BleState.SCANNING
                    }

                    BleService.ACTION_SCAN_STOPPED -> {
                        Log.d(TAG, "Ble scan stopped")
                        if (_state.value == BleState.SCANNING) {
                            _state.value = BleState.DISCONNECTED
                        }
                    }

                    BleService.ACTION_ERROR -> {
                        val error = intent.getStringExtra(BleService.EXTRA_ERROR)
                        Log.e(TAG, "Ble error: $error")
                        scope.launch {
                            _error.emit(error ?: "Unknown error")
                        }
                    }

                    BleService.ACTION_BLUETOOTH_STATE_CHANGED -> {
                        val state = intent.getIntExtra(BleService.EXTRA_BLUETOOTH_STATE, -1)
                        Log.d(TAG, "Bluetooth state changed: $state")
                        if (state == BluetoothAdapter.STATE_OFF) {
                            _state.value = BleState.DISCONNECTED
                        }
                    }
                }
            }
        }

        broadcastReceiver = receiver

        val filter = IntentFilter().apply {
            addAction(BleService.ACTION_DEVICE_FOUND)
            addAction(BleService.ACTION_METRICS_UPDATE)
            addAction(BleService.ACTION_CONNECTED)
            addAction(BleService.ACTION_DISCONNECTED)
            addAction(BleService.ACTION_SCAN_STARTED)
            addAction(BleService.ACTION_SCAN_STOPPED)
            addAction(BleService.ACTION_ERROR)
            addAction(BleService.ACTION_BLUETOOTH_STATE_CHANGED)
        }

        LocalBroadcastManager.getInstance(context).registerReceiver(receiver, filter)
        Log.d(TAG, "Broadcast receiver registered")
    }

    fun startScan() {
        Log.d(TAG, "startScan called")
        if (isServiceBound && bleService != null) {
            bleService?.startScan()
        } else {
            Log.w(TAG, "Cannot start scan - service not bound")
        }
    }

    fun stopScan() {
        Log.d(TAG, "stopScan called")
        if (isServiceBound && bleService != null) {
            bleService?.stopScan()
        } else {
            Log.w(TAG, "Cannot stop scan - service not bound")
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun connectToDevice(device: WheelDevice) {
        Log.d(TAG, "connectToDevice called: ${device.name}")
        if (isServiceBound && bleService != null) {
            bleService?.connectToDevice(device)
            pendingDevice = null
        } else {
            Log.d(TAG, "Service not bound, saving device for later")
            pendingDevice = device
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun disconnect() {
        Log.d(TAG, "disconnect called")
        if (isServiceBound && bleService != null) {
            bleService?.disconnect()
        }
        pendingDevice = null
    }

    fun isConnected(): Boolean {
        return _state.value == BleState.CONNECTED
    }

    fun isScanning(): Boolean {
        return _state.value == BleState.SCANNING
    }

    override suspend fun start() {
        Log.d(TAG, "start called")
        if (_state.value == BleState.DISCONNECTED) {
            startScan()
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override suspend fun stop() {
        Log.d(TAG, "stop called")
        disconnect()
        stopScan()
    }

    fun cleanup() {
        Log.d(TAG, "cleanup called")
        broadcastReceiver?.let { receiver ->
            try {
                LocalBroadcastManager.getInstance(context).unregisterReceiver(receiver)
                Log.d(TAG, "Broadcast receiver unregistered")
            } catch (e: IllegalArgumentException) {
                Log.e(TAG, "Error unregistering receiver: ${e.message}")
            }
        }

        if (isServiceBound) {
            try {
                context.unbindService(serviceConnection)
                isServiceBound = false
                Log.d(TAG, "Service unbound")
            } catch (e: Exception) {
                Log.e(TAG, "Error unbinding service: ${e.message}")
            }
        }

        bleService = null
        pendingDevice = null
        scope.cancel()
    }
}