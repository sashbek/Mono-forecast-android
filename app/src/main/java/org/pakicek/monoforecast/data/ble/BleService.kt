package org.pakicek.monoforecast.data.ble

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
import android.content.Intent
import android.content.IntentFilter
import android.os.Binder
import android.os.IBinder
import androidx.annotation.RequiresPermission
import androidx.core.app.ActivityCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.pakicek.monoforecast.data.ble.protocol.TestWheelAdapter
import org.pakicek.monoforecast.data.ble.protocol.WheelProtocolAdapter
import org.pakicek.monoforecast.domain.model.ble.WheelDevice
import java.util.UUID

class BleService : Service() {

    inner class LocalBinder : Binder() {
        fun getService(): BleService = this@BleService
    }

    private val binder = LocalBinder()
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private var bluetoothAdapter: BluetoothAdapter? = null
    private var bluetoothLeScanner: BluetoothLeScanner? = null
    private var bluetoothGatt: BluetoothGatt? = null

    private var scanning = false
    private var connected = false
    private var currentAdapter: WheelProtocolAdapter? = null

    private val bluetoothStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: android.content.Context?, intent: Intent?) {
            if (BluetoothAdapter.ACTION_STATE_CHANGED == intent?.action) {
                when (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)) {
                    BluetoothAdapter.STATE_OFF -> {
                        disconnect()
                        stopScan()
                    }
                    BluetoothAdapter.STATE_ON -> initBluetooth()
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        initBluetooth()
        registerReceiver(bluetoothStateReceiver, IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED))
    }

    override fun onBind(intent: Intent?): IBinder = binder

    fun isScanning(): Boolean = scanning
    fun isConnected(): Boolean = connected

    private fun initBluetooth() {
        val bluetoothManager = getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter
        bluetoothLeScanner = bluetoothAdapter?.bluetoothLeScanner
    }

    fun startScan() {
        if (!isBluetoothEnabled()) {
            sendError("Bluetooth is disabled")
            return
        }
        if (!hasScanPermission()) {
            sendError("Bluetooth scan permission denied")
            return
        }
        if (scanning) return

        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()

        scanning = true
        BleEventBus.tryEmit(BleEventBus.Event.ScanStarted)

        try {
            bluetoothLeScanner?.startScan(emptyList<ScanFilter>(), settings, scanCallback)
        } catch (e: SecurityException) {
            scanning = false
            sendError("No scan permission")
        } catch (e: Exception) {
            scanning = false
            sendError(e.message ?: "Scan start failed")
        }
    }

    fun stopScan() {
        if (!scanning) return
        try {
            if (hasScanPermission()) {
                bluetoothLeScanner?.stopScan(scanCallback)
            }
        } catch (_: Exception) {
        }
        scanning = false
        BleEventBus.tryEmit(BleEventBus.Event.ScanStopped)
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun connectToDevice(device: WheelDevice) {
        val btDevice = device.bluetoothDevice ?: run {
            sendError("Invalid device")
            return
        }

        if (!hasConnectPermission()) {
            sendError("Bluetooth connect permission denied")
            return
        }

        stopScan()

        currentAdapter = when (device.adapterType) {
            "test" -> TestWheelAdapter()
            else -> TestWheelAdapter()
        }

        connected = false

        try {
            bluetoothGatt = btDevice.connectGatt(this, false, gattCallback)
        } catch (e: SecurityException) {
            sendError("No connect permission")
        } catch (e: Exception) {
            sendError(e.message ?: "Connection failed")
        }
    }

    fun disconnect() {
        try {
            bluetoothGatt?.disconnect()
            bluetoothGatt?.close()
        } catch (_: Exception) {
        }
        bluetoothGatt = null
        currentAdapter = null
        connected = false
        BleEventBus.tryEmit(BleEventBus.Event.Disconnected)
    }

    private val scanCallback = object : ScanCallback() {
        @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            val device = result?.device ?: return
            val safeName = getDeviceNameSafe(device)

            val wheelDevice = WheelDevice(
                address = device.address,
                name = safeName,
                rssi = result.rssi,
                bondState = getBondStateSafe(device),
                adapterType = identifyAdapterType(safeName),
                bluetoothDevice = device
            )

            BleEventBus.tryEmit(BleEventBus.Event.DeviceFound(wheelDevice))
        }
    }

    private val gattCallback = object : BluetoothGattCallback() {

        @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    try {
                        gatt.discoverServices()
                    } catch (e: Exception) {
                        sendError("Service discovery failed")
                    }
                }

                BluetoothProfile.STATE_DISCONNECTED -> {
                    disconnect()
                }
            }
        }

        @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                connected = true
                BleEventBus.tryEmit(BleEventBus.Event.Connected)
                setupNotifications(gatt)
            } else {
                sendError("Service discovery failed")
            }
        }

        override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
            val data = characteristic.value ?: return
            val adapter = currentAdapter ?: return

            scope.launch {
                adapter.parseData(data)?.let {
                    BleEventBus.emit(BleEventBus.Event.MetricsUpdate(it))
                }
            }
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private fun setupNotifications(gatt: BluetoothGatt) {
        val serviceUuids = listOf(
            UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb"),
            UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e"),
            UUID.fromString("0000fff0-0000-1000-8000-00805f9b34fb")
        )

        val characteristicUuids = listOf(
            UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb"),
            UUID.fromString("6e400003-b5a3-f393-e0a9-e50e24dcca9e"),
            UUID.fromString("0000fff1-0000-1000-8000-00805f9b34fb")
        )

        for (serviceUuid in serviceUuids) {
            val service = gatt.getService(serviceUuid) ?: continue
            for (charUuid in characteristicUuids) {
                val characteristic = service.getCharacteristic(charUuid) ?: continue
                if (characteristic.properties and BluetoothGattCharacteristic.PROPERTY_NOTIFY != 0) {
                    gatt.setCharacteristicNotification(characteristic, true)
                    val descriptor = characteristic.getDescriptor(
                        UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")
                    )
                    descriptor?.let {
                        it.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                        gatt.writeDescriptor(it)
                        return
                    }
                }
            }
        }

        sendError("No notification characteristic found")
    }

    private fun sendError(message: String) {
        BleEventBus.tryEmit(BleEventBus.Event.Error(message))
    }

    private fun isBluetoothEnabled(): Boolean = bluetoothAdapter?.isEnabled == true

    private fun hasScanPermission(): Boolean {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_SCAN
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        } else {
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        }
    }

    private fun hasConnectPermission(): Boolean {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_CONNECT
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        } else {
            true
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
        } catch (_: Exception) {
            "Unknown_${device.address.takeLast(4)}"
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
        } catch (_: Exception) {
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

    override fun onDestroy() {
        disconnect()
        stopScan()
        try {
            unregisterReceiver(bluetoothStateReceiver)
        } catch (_: Exception) {
        }
        scope.cancel()
        super.onDestroy()
    }
}