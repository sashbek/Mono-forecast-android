package org.pakicek.monoforecast.data.features.ble

import android.Manifest
import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.core.app.ActivityCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.pakicek.monoforecast.data.features.ble.adapters.TestWheelAdapter
import org.pakicek.monoforecast.data.features.ble.adapters.WheelProtocolAdapter
import org.pakicek.monoforecast.domain.model.dto.ble.WheelDevice
import org.pakicek.monoforecast.domain.model.dto.ble.WheelMetrics
import java.util.UUID

class BleService : Service() {

    companion object {
        const val TAG = "BLEService"
        const val ACTION_DEVICE_FOUND = "org.pakicek.monoforecast.DEVICE_FOUND"
        const val ACTION_SCAN_STARTED = "org.pakicek.monoforecast.SCAN_STARTED"
        const val ACTION_SCAN_STOPPED = "org.pakicek.monoforecast.SCAN_STOPPED"
        const val ACTION_CONNECTED = "org.pakicek.monoforecast.CONNECTED"
        const val ACTION_DISCONNECTED = "org.pakicek.monoforecast.DISCONNECTED"
        const val ACTION_METRICS_UPDATE = "org.pakicek.monoforecast.METRICS_UPDATE"
        const val ACTION_ERROR = "org.pakicek.monoforecast.ERROR"
        const val ACTION_BLUETOOTH_STATE_CHANGED = "org.pakicek.monoforecast.BLUETOOTH_STATE_CHANGED"

        const val EXTRA_DEVICE = "device"
        const val EXTRA_METRICS = "metrics"
        const val EXTRA_ERROR = "error"
        const val EXTRA_BLUETOOTH_STATE = "bluetooth_state"
    }

    private val binder = LocalBinder()
    private var bluetoothAdapter: BluetoothAdapter? = null
    private var bluetoothLeScanner: BluetoothLeScanner? = null
    private var bluetoothGatt: BluetoothGatt? = null
    private var isScanning = false
    private var isConnectedFlag = false  // Добавляем флаг подключения

    private var currentDevice: WheelDevice? = null
    private var currentAdapter: WheelProtocolAdapter? = null

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    // LocalBroadcastManager instance
    private lateinit var localBroadcastManager: LocalBroadcastManager

    private val bluetoothStateReceiver = object : BroadcastReceiver() {
        @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                BluetoothAdapter.ACTION_STATE_CHANGED -> {
                    val state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)
                    val broadcastIntent = Intent(ACTION_BLUETOOTH_STATE_CHANGED)
                    broadcastIntent.putExtra(EXTRA_BLUETOOTH_STATE, state)
                    localBroadcastManager.sendBroadcast(broadcastIntent)

                    when (state) {
                        BluetoothAdapter.STATE_OFF -> {
                            Log.d(TAG, "Bluetooth turned off")
                            disconnect()
                            stopScan()
                        }
                        BluetoothAdapter.STATE_ON -> {
                            Log.d(TAG, "Bluetooth turned on")
                            initBluetooth()
                        }
                    }
                }
            }
        }
    }

    inner class LocalBinder : Binder() {
        fun getService(): BleService = this@BleService
    }

    override fun onCreate() {
        super.onCreate()
        localBroadcastManager = LocalBroadcastManager.getInstance(this)
        initBluetooth()
        registerBluetoothStateReceiver()
        Log.d(TAG, "BleService created")
    }

    private fun registerBluetoothStateReceiver() {
        val filter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        registerReceiver(bluetoothStateReceiver, filter)
    }

    private fun initBluetooth() {
        val bluetoothManager = getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter
        bluetoothLeScanner = bluetoothAdapter?.bluetoothLeScanner
        Log.d(TAG, "Bluetooth initialized, enabled: ${bluetoothAdapter?.isEnabled}")
    }

    fun isBluetoothEnabled(): Boolean = bluetoothAdapter?.isEnabled == true

    // Исправленный метод isConnected()
    fun isConnected(): Boolean = isConnectedFlag

    private fun hasScanPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED
        } else {
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun hasConnectPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun startScan() {
        Log.d(TAG, "startScan called, isBluetoothEnabled: ${isBluetoothEnabled()}, isScanning: $isScanning")

        if (!isBluetoothEnabled()) {
            sendError("Bluetooth is disabled")
            return
        }

        if (isScanning) {
            Log.d(TAG, "Already scanning")
            return
        }

        if (!hasScanPermission()) {
            sendError("Bluetooth scan permission not granted")
            return
        }

        val scanSettings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()

        val scanFilters = listOf<ScanFilter>()

        try {
            bluetoothLeScanner?.startScan(scanFilters, scanSettings, scanCallback)
            isScanning = true
            localBroadcastManager.sendBroadcast(Intent(ACTION_SCAN_STARTED))
            Log.d(TAG, "Scan started")
        } catch (e: SecurityException) {
            sendError("Security exception: ${e.message}")
            isScanning = false
        } catch (e: Exception) {
            sendError("Error starting scan: ${e.message}")
            isScanning = false
        }
    }

    fun stopScan() {
        Log.d(TAG, "stopScan called, isScanning: $isScanning")

        if (!isScanning) return

        if (!hasScanPermission()) {
            Log.e(TAG, "Cannot stop scan - no permission")
            isScanning = false
            return
        }

        try {
            bluetoothLeScanner?.stopScan(scanCallback)
            isScanning = false
            localBroadcastManager.sendBroadcast(Intent(ACTION_SCAN_STOPPED))
            Log.d(TAG, "Scan stopped")
        } catch (e: SecurityException) {
            Log.e(TAG, "Error stopping scan: ${e.message}")
            isScanning = false
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping scan: ${e.message}")
            isScanning = false
        }
    }

    private val scanCallback = object : ScanCallback() {
        @RequiresPermission(value = "android.permission.BLUETOOTH_CONNECT")
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            result?.let {
                val device = it.device
                val rssi = it.rssi
                val deviceName = device.name ?: "Unknown"

                val safeDeviceName = getDeviceNameSafe(device)
                val bondState = getBondStateSafe(device)

                val wheelDevice = WheelDevice(
                    address = device.address,
                    name = safeDeviceName,
                    rssi = rssi,
                    bondState = bondState,
                    bluetoothDevice = device
                )

                wheelDevice.adapterType = identifyAdapterType(safeDeviceName)
                sendDeviceFound(wheelDevice)
            }
        }

        @RequiresPermission(value = "android.permission.BLUETOOTH_CONNECT")
        override fun onBatchScanResults(results: MutableList<ScanResult>?) {
            results?.forEach { onScanResult(ScanSettings.CALLBACK_TYPE_ALL_MATCHES, it) }
        }

        override fun onScanFailed(errorCode: Int) {
            Log.e(TAG, "onScanFailed! errorCode: $errorCode")
            when (errorCode) {
                SCAN_FAILED_ALREADY_STARTED -> Log.e(TAG, "Scan failed: already started")
                SCAN_FAILED_APPLICATION_REGISTRATION_FAILED -> Log.e(TAG, "Scan failed: application registration failed")
                SCAN_FAILED_FEATURE_UNSUPPORTED -> Log.e(TAG, "Scan failed: feature unsupported")
                SCAN_FAILED_INTERNAL_ERROR -> Log.e(TAG, "Scan failed: internal error")
                else -> Log.e(TAG, "Scan failed: unknown error")
            }
            sendError("Scan failed with error: $errorCode")
            isScanning = false
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private fun getDeviceNameSafe(device: BluetoothDevice): String {
        return try {
            if (hasConnectPermission()) {
                device.name ?: "Unknown"
            } else {
                "Device_${device.address.takeLast(4)}"
            }
        } catch (e: SecurityException) {
            "Unknown_${device.address.takeLast(4)}"
        } catch (e: Exception) {
            "Unknown"
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private fun getBondStateSafe(device: BluetoothDevice): Int {
        return try {
            if (hasConnectPermission()) {
                device.bondState
            } else {
                BluetoothDevice.BOND_NONE
            }
        } catch (e: SecurityException) {
            BluetoothDevice.BOND_NONE
        } catch (e: Exception) {
            BluetoothDevice.BOND_NONE
        }
    }

    private fun identifyAdapterType(deviceName: String?): String {
        return when {
            deviceName.isNullOrEmpty() -> "unknown"
            deviceName.contains("TEST", ignoreCase = true) -> "test"
            deviceName.contains("KS", ignoreCase = true) -> "kingsong"
            deviceName.contains("INMOTION", ignoreCase = true) -> "inmotion"
            deviceName.contains("GW", ignoreCase = true) -> "gotway"
            deviceName.contains("Begode", ignoreCase = true) -> "gotway"
            deviceName.contains("Ninebot", ignoreCase = true) -> "ninebot"
            else -> "unknown"
        }
    }

    private fun sendDeviceFound(device: WheelDevice) {
        Log.d(TAG, "sendDeviceFound called for device: ${device.name}")
        val intent = Intent(ACTION_DEVICE_FOUND)
        intent.putExtra(EXTRA_DEVICE, device)
        localBroadcastManager.sendBroadcast(intent)
        Log.d(TAG, "LocalBroadcast sent for device: ${device.name}")
    }

    private fun sendMetricsUpdate(metrics: WheelMetrics) {
        val intent = Intent(ACTION_METRICS_UPDATE)
        intent.putExtra(EXTRA_METRICS, metrics)
        localBroadcastManager.sendBroadcast(intent)
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun connectToDevice(device: WheelDevice) {
        Log.d(TAG, "connectToDevice called: ${device.name}")

        if (device.bluetoothDevice == null) {
            sendError("Invalid device")
            return
        }

        if (!hasConnectPermission()) {
            sendError("Bluetooth connect permission not granted")
            return
        }

        stopScan()
        currentDevice = device

        currentAdapter = when (device.adapterType) {
            "test" -> TestWheelAdapter()
            else -> TestWheelAdapter()
        }

        try {
            bluetoothGatt = device.bluetoothDevice?.connectGatt(this, false, gattCallback)
            Log.d(TAG, "Connecting to device: ${device.name}")
        } catch (e: SecurityException) {
            sendError("Security exception while connecting: ${e.message}")
        } catch (e: Exception) {
            sendError("Error connecting: ${e.message}")
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun disconnect() {
        Log.d(TAG, "disconnect called")
        try {
            if (bluetoothGatt != null) {
                if (hasConnectPermission()) {
                    bluetoothGatt?.disconnect()
                }
                bluetoothGatt?.close()
                bluetoothGatt = null
            }
            currentDevice = null
            currentAdapter = null
            isConnectedFlag = false
            localBroadcastManager.sendBroadcast(Intent(ACTION_DISCONNECTED))
            Log.d(TAG, "Disconnected")
        } catch (e: Exception) {
            Log.e(TAG, "Error disconnecting: ${e.message}")
        }
    }

    private val gattCallback = object : BluetoothGattCallback() {
        @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            Log.d(TAG, "onConnectionStateChange - status: $status, newState: $newState")

            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    Log.d(TAG, "Connected to GATT server")
                    isConnectedFlag = true
                    localBroadcastManager.sendBroadcast(Intent(ACTION_CONNECTED))

                    if (hasConnectPermission()) {
                        try {
                            gatt.discoverServices()
                        } catch (e: SecurityException) {
                            sendError("Security exception during service discovery: ${e.message}")
                        }
                    }
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    Log.d(TAG, "Disconnected from GATT server")
                    isConnectedFlag = false
                    disconnect()
                }
                BluetoothProfile.STATE_CONNECTING -> {
                    Log.d(TAG, "Connecting to GATT server...")
                }
                BluetoothProfile.STATE_DISCONNECTING -> {
                    Log.d(TAG, "Disconnecting from GATT server...")
                }
            }
        }

        @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            Log.d(TAG, "onServicesDiscovered - status: $status")
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "Services discovered")
                setupNotifications()
            } else {
                Log.e(TAG, "Service discovery failed with status: $status")
                sendError("Service discovery failed")
                disconnect()
            }
        }

        override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
            val data = characteristic.value
            if (data != null && currentAdapter != null) {
                scope.launch {
                    try {
                        val metrics = currentAdapter?.parseData(data)
                        metrics?.let {
                            sendMetricsUpdate(it)
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing data: ${e.message}")
                    }
                }
            }
        }

        override fun onCharacteristicRead(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                val data = characteristic.value
                if (data != null && currentAdapter != null) {
                    scope.launch {
                        try {
                            val metrics = currentAdapter?.parseData(data)
                            metrics?.let {
                                sendMetricsUpdate(it)
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Error parsing data: ${e.message}")
                        }
                    }
                }
            }
        }

        override fun onDescriptorWrite(gatt: BluetoothGatt, descriptor: BluetoothGattDescriptor, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "Descriptor written successfully")
            } else {
                Log.e(TAG, "Failed to write descriptor, status: $status")
            }
        }

        override fun onMtuChanged(gatt: BluetoothGatt, mtu: Int, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "MTU changed to: $mtu")
            } else {
                Log.e(TAG, "Failed to change MTU, status: $status")
            }
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private fun setupNotifications() {
        if (!hasConnectPermission()) {
            Log.e(TAG, "Cannot setup notifications - no permission")
            return
        }

        bluetoothGatt?.let { gatt ->
            try {
                val possibleServiceUuids = listOf(
                    UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb"),
                    UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e"),
                    UUID.fromString("0000fff0-0000-1000-8000-00805f9b34fb")
                )

                val possibleCharUuids = listOf(
                    UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb"),
                    UUID.fromString("6e400003-b5a3-f393-e0a9-e50e24dcca9e"),
                    UUID.fromString("0000fff1-0000-1000-8000-00805f9b34fb")
                )

                var notificationSetup = false

                for (serviceUuid in possibleServiceUuids) {
                    val service = gatt.getService(serviceUuid)
                    service?.let {
                        for (charUuid in possibleCharUuids) {
                            val characteristic = it.getCharacteristic(charUuid)
                            characteristic?.let { char ->
                                val properties = char.properties
                                if (properties and BluetoothGattCharacteristic.PROPERTY_NOTIFY != 0) {
                                    gatt.setCharacteristicNotification(char, true)

                                    val descriptor = char.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"))
                                    descriptor?.let {
                                        it.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                                        gatt.writeDescriptor(it)
                                        notificationSetup = true
                                        Log.d(TAG, "Notifications enabled for: ${char.uuid}")
                                    }
                                }
                            }
                        }
                    }
                }

                if (!notificationSetup) {
                    Log.w(TAG, "No notification characteristics found")
                    readCharacteristics()
                }

            } catch (e: SecurityException) {
                Log.e(TAG, "Security exception setting up notifications: ${e.message}")
            } catch (e: Exception) {
                Log.e(TAG, "Error setting up notifications: ${e.message}")
            }
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private fun readCharacteristics() {
        bluetoothGatt?.let { gatt ->
            try {
                val services = gatt.services
                for (service in services) {
                    val characteristics = service.characteristics
                    for (characteristic in characteristics) {
                        val properties = characteristic.properties
                        if (properties and BluetoothGattCharacteristic.PROPERTY_READ != 0) {
                            gatt.readCharacteristic(characteristic)
                            Log.d(TAG, "Reading characteristic: ${characteristic.uuid}")
                        }
                    }
                }
            } catch (e: SecurityException) {
                Log.e(TAG, "Security exception reading characteristics: ${e.message}")
            } catch (e: Exception) {
                Log.e(TAG, "Error reading characteristics: ${e.message}")
            }
        }
    }

    private fun sendError(message: String) {
        Log.e(TAG, message)
        val intent = Intent(ACTION_ERROR)
        intent.putExtra(EXTRA_ERROR, message)
        localBroadcastManager.sendBroadcast(intent)
    }

    override fun onBind(intent: Intent?): IBinder = binder

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override fun onDestroy() {
        Log.d(TAG, "BleService onDestroy")
        super.onDestroy()
        disconnect()
        stopScan()
        scope.cancel()
        try {
            unregisterReceiver(bluetoothStateReceiver)
        } catch (e: IllegalArgumentException) {
            Log.e(TAG, "Receiver not registered")
        }
    }
}