package org.pakicek.monoforecast.presentation.ble

import android.Manifest
import android.annotation.SuppressLint
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.pakicek.monoforecast.data.ble.BleFeature
import org.pakicek.monoforecast.domain.model.VehicleMetric
import org.pakicek.monoforecast.domain.model.ble.WheelDevice
import org.pakicek.monoforecast.domain.model.ble.WheelMetrics
import org.pakicek.monoforecast.logic.service.MainService

class BluetoothViewModel : ViewModel() {

    companion object {
        private const val TAG = "BluetoothViewModel"
    }

    private val _currentSpeed = MutableLiveData(0f)
    val currentSpeed: LiveData<Float> = _currentSpeed

    private val _metrics = MutableLiveData<List<VehicleMetric>>()
    val metrics: LiveData<List<VehicleMetric>> = _metrics

    private val _connectionStatus = MutableLiveData("Disconnected")
    val connectionStatus: LiveData<String> = _connectionStatus

    private val _devices = MutableLiveData<List<WheelDevice>>(emptyList())
    val devices: LiveData<List<WheelDevice>> = _devices

    private val _isScanning = MutableLiveData(false)
    val isScanning: LiveData<Boolean> = _isScanning

    private val _isConnected = MutableLiveData(false)
    val isConnected: LiveData<Boolean> = _isConnected

    private var bleFeature: BleFeature? = null

    init {
        subscribeToBleData()
    }

    private fun subscribeToBleData() {
        viewModelScope.launch {
            MainService.deviceFound?.collect { device ->
                Log.d(TAG, "Device found: ${device.name}")
                addDeviceDirectly(device)
            }
        }

        viewModelScope.launch {
            MainService.metricsUpdate?.collect { metrics ->
                Log.d(TAG, "Metrics update received")
                updateMetricsDirectly(metrics)
            }
        }

        viewModelScope.launch {
            MainService.error?.collect { error ->
                Log.e(TAG, "BLE error: $error")
                onError(error)
            }
        }
    }

    fun setBLEService(bleFeature: BleFeature?) {
        this.bleFeature = bleFeature
    }

    fun startScan() {
        Log.d(TAG, "startScan called")
        bleFeature?.startScan()
        _isScanning.value = true
        _connectionStatus.value = "Scanning for devices..."
    }

    fun stopScan() {
        Log.d(TAG, "stopScan called")
        bleFeature?.stopScan()
        _isScanning.value = false
        _connectionStatus.value = "Scan stopped"
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun connectToDevice(device: WheelDevice) {
        Log.d(TAG, "connectToDevice called: ${device.name}")
        bleFeature?.connectToDevice(device)
        _connectionStatus.value = "Connecting to ${device.name}..."
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun disconnect() {
        Log.d(TAG, "disconnect called")
        bleFeature?.disconnect()
        _connectionStatus.value = "Disconnecting..."
    }

    fun addDeviceDirectly(device: WheelDevice) {
        Log.d(TAG, "addDeviceDirectly: ${device.name}")
        val currentList = _devices.value?.toMutableList() ?: mutableListOf()

        val existingIndex = currentList.indexOfFirst { it.address == device.address }
        if (existingIndex != -1) {
            currentList[existingIndex] = device
        } else {
            currentList.add(device)
        }

        _devices.value = currentList
    }

    @SuppressLint("DefaultLocale")
    fun updateMetricsDirectly(metrics: WheelMetrics) {
        Log.d(TAG, "updateMetricsDirectly called")

        val vehicleMetrics = mutableListOf<VehicleMetric>()

        metrics.speed.let {
            vehicleMetrics.add(VehicleMetric("SPEED", String.format("%.1f", it), "km/h"))
            _currentSpeed.value = it
        }

        metrics.voltage.let {
            vehicleMetrics.add(VehicleMetric("VOLTAGE", String.format("%.1f", it), "V"))
        }

        metrics.current.let {
            vehicleMetrics.add(VehicleMetric("CURRENT", String.format("%.1f", it), "A"))
        }

        metrics.distance.let {
            vehicleMetrics.add(VehicleMetric("DISTANCE", String.format("%.1f", it), "km"))
        }

        metrics.temperature.let {
            vehicleMetrics.add(VehicleMetric("TEMP", it.toString(), "°C"))
        }

        metrics.batteryLevel.let {
            vehicleMetrics.add(VehicleMetric("BATTERY", it.toString(), "%"))
        }

        _metrics.value = vehicleMetrics
    }

    fun onScanStarted() {
        Log.d(TAG, "onScanStarted called")
        _isScanning.value = true
        _connectionStatus.value = "Scanning for devices..."
    }

    fun onScanStopped() {
        Log.d(TAG, "onScanStopped called")
        _isScanning.value = false
        if (_isConnected.value != true) {
            _connectionStatus.value = "Scan stopped"
        }
    }

    fun onConnected() {
        Log.d(TAG, "onConnected called")
        _isConnected.value = true
        _isScanning.value = false
        _connectionStatus.value = "Connected"
    }

    fun onDisconnected() {
        Log.d(TAG, "onDisconnected called")
        _isConnected.value = false
        _connectionStatus.value = "Disconnected"
        _currentSpeed.value = 0f
        _metrics.value = emptyList()
    }

    fun onError(message: String?) {
        Log.e(TAG, "onError: $message")
        _connectionStatus.value = "Error: ${message ?: "Unknown error"}"
    }

    fun cleanup() {
        Log.d(TAG, "cleanup called")
        bleFeature = null
    }
}