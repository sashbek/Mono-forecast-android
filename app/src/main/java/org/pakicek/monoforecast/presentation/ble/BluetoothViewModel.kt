package org.pakicek.monoforecast.presentation.ble

import android.Manifest
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.pakicek.monoforecast.domain.model.dto.VehicleMetric
import org.pakicek.monoforecast.presentation.ble.connection.models.WheelDevice
import org.pakicek.monoforecast.presentation.ble.connection.models.WheelMetrics
import org.pakicek.monoforecast.presentation.ble.connection.service.BLEService

class BluetoothViewModel : ViewModel() {

    companion object {
        private const val TAG = "BluetoothViewModel"
    }

    private var bleService: BLEService? = null

    // LiveData для UI
    private val _currentSpeed = MutableLiveData<Float>()
    val currentSpeed: LiveData<Float> = _currentSpeed

    private val _metrics = MutableLiveData<List<VehicleMetric>>()
    val metrics: LiveData<List<VehicleMetric>> = _metrics

    private val _connectionStatus = MutableLiveData<String>()
    val connectionStatus: LiveData<String> = _connectionStatus

    private val _devices = MutableLiveData<List<WheelDevice>>()
    val devices: LiveData<List<WheelDevice>> = _devices

    private val _isScanning = MutableLiveData<Boolean>()
    val isScanning: LiveData<Boolean> = _isScanning

    private val _isConnected = MutableLiveData<Boolean>()
    val isConnected: LiveData<Boolean> = _isConnected

    private val devicesList = mutableListOf<WheelDevice>()

    fun setBLEService(service: BLEService?) {
        bleService = service
    }

    // Прямые методы для обновления из Activity
    fun addDeviceDirectly(device: WheelDevice) {
        Log.d(TAG, "addDeviceDirectly called for: ${device.name}")
        addDevice(device)
    }

    fun updateMetricsDirectly(metrics: WheelMetrics) {
        Log.d(TAG, "updateMetricsDirectly called")
        updateMetrics(metrics)
    }

    fun onScanStarted() {
        Log.d(TAG, "onScanStarted called")
        _isScanning.postValue(true)
        _connectionStatus.postValue("Scanning for devices...")
        devicesList.clear()
        _devices.postValue(emptyList())
    }

    fun onScanStopped() {
        Log.d(TAG, "onScanStopped called")
        _isScanning.postValue(false)
        _connectionStatus.postValue("Scan stopped")
    }

    fun onConnected() {
        Log.d(TAG, "onConnected called")
        _isConnected.postValue(true)
        _connectionStatus.postValue("Connected to device")
    }

    fun onDisconnected() {
        Log.d(TAG, "onDisconnected called")
        _isConnected.postValue(false)
        _connectionStatus.postValue("Disconnected")
        clearMetrics()
    }

    fun onError(error: String?) {
        Log.d(TAG, "onError called: $error")
        _connectionStatus.postValue("Error: $error")
    }

    private fun addDevice(device: WheelDevice) {
        Log.d(TAG, "addDevice called for: ${device.name} (${device.address})")
        if (!devicesList.any { it.address == device.address }) {
            devicesList.add(device)
            _devices.postValue(devicesList.toList())
            Log.d(TAG, "Device added, total devices: ${devicesList.size}")
            Log.d(TAG, "Devices list: ${devicesList.map { it.name }}")
        } else {
            Log.d(TAG, "Device already in list")
        }
    }

    private fun updateMetrics(metrics: WheelMetrics) {
        _currentSpeed.postValue(metrics.speed)

        val vehicleMetrics = listOf(
            VehicleMetric("Speed", "${String.format("%.1f", metrics.speed)} km/h", "km/h", "⚡"),
            VehicleMetric("Battery", "${metrics.batteryLevel}%", "%", "🔋"),
            VehicleMetric("Voltage", "${String.format("%.1f", metrics.voltage)} V", "V", "⚡"),
            VehicleMetric("Current", "${String.format("%.1f", metrics.current)} A", "A", "💨"),
            VehicleMetric("Temperature", "${String.format("%.1f", metrics.temperature)}°C", "°C", "🌡️"),
            VehicleMetric("Distance", "${String.format("%.1f", metrics.distance)} km", "km", "📏"),
            VehicleMetric("Odometer", "${String.format("%.1f", metrics.odometer)} km", "km", "📊"),
            VehicleMetric("Error Code", "${metrics.errorCode}", "", "⚠️")
        )

        _metrics.postValue(vehicleMetrics)
        _connectionStatus.postValue("Receiving data")
    }

    private fun clearMetrics() {
        val emptyMetrics = listOf(
            VehicleMetric("Speed", "-- km/h", "km/h", "⚡"),
            VehicleMetric("Battery", "--%", "%", "🔋"),
            VehicleMetric("Voltage", "-- V", "V", "⚡"),
            VehicleMetric("Current", "-- A", "A", "💨"),
            VehicleMetric("Temperature", "--°C", "°C", "🌡️"),
            VehicleMetric("Distance", "-- km", "km", "📏"),
            VehicleMetric("Odometer", "-- km", "km", "📊"),
            VehicleMetric("Error Code", "--", "", "⚠️")
        )
        _metrics.postValue(emptyMetrics)
        _currentSpeed.postValue(0f)
    }

    fun startScan() {
        Log.d(TAG, "startScan called")
        bleService?.startScan()
    }

    fun stopScan() {
        Log.d(TAG, "stopScan called")
        bleService?.stopScan()
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun connectToDevice(device: WheelDevice) {
        Log.d(TAG, "connectToDevice called: ${device.name}")
        bleService?.connectToDevice(device)
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun disconnect() {
        Log.d(TAG, "disconnect called")
        bleService?.disconnect()
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun cleanup() {
        Log.d(TAG, "cleanup called")
        disconnect()
        stopScan()
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override fun onCleared() {
        super.onCleared()
        Log.d(TAG, "onCleared")
        cleanup()
    }
}