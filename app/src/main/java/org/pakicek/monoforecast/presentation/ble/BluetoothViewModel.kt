package org.pakicek.monoforecast.presentation.ble

import android.Manifest
import android.content.Context
import androidx.annotation.RequiresPermission
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.pakicek.monoforecast.data.ble.BleFeature
import org.pakicek.monoforecast.domain.model.BleState
import org.pakicek.monoforecast.domain.model.VehicleMetric
import org.pakicek.monoforecast.domain.model.ble.WheelDevice
import org.pakicek.monoforecast.domain.model.ble.WheelMetrics

class BluetoothViewModel : ViewModel() {

    private val _currentSpeed = MutableLiveData(0f)
    val currentSpeed: LiveData<Float> = _currentSpeed

    private val _metrics = MutableLiveData<List<VehicleMetric>>(emptyList())
    val metrics: LiveData<List<VehicleMetric>> = _metrics

    private val _connectionStatus = MutableLiveData("Waiting for Bluetooth...")
    val connectionStatus: LiveData<String> = _connectionStatus

    private val _devices = MutableLiveData<List<WheelDevice>>(emptyList())
    val devices: LiveData<List<WheelDevice>> = _devices

    private val _isScanning = MutableLiveData(false)
    val isScanning: LiveData<Boolean> = _isScanning

    private val _isConnected = MutableLiveData(false)
    val isConnected: LiveData<Boolean> = _isConnected

    private var bleFeature: BleFeature? = null

    private var stateJob: Job? = null
    private var devicesJob: Job? = null
    private var metricsJob: Job? = null
    private var errorJob: Job? = null

    fun attach(context: Context) {
        if (bleFeature != null) return

        val feature = BleFeature(context.applicationContext)
        bleFeature = feature

        stateJob = viewModelScope.launch {
            feature.state.collectLatest { state ->
                when (state) {
                    BleState.DISCONNECTED -> {
                        _isScanning.value = false
                        _isConnected.value = false
                        _connectionStatus.value = "Disconnected"
                        _currentSpeed.value = 0f
                        _metrics.value = emptyList()
                    }

                    BleState.CONNECTING -> {
                        _isScanning.value = false
                        _isConnected.value = false
                        _connectionStatus.value = "Connecting..."
                    }

                    BleState.CONNECTED -> {
                        _isScanning.value = false
                        _isConnected.value = true
                        _connectionStatus.value = "Connected"
                    }

                    BleState.SCANNING -> {
                        _isScanning.value = true
                        _isConnected.value = false
                        _connectionStatus.value = "Scanning..."
                    }
                }
            }
        }

        devicesJob = viewModelScope.launch {
            feature.deviceFound.collectLatest { device ->
                val current = _devices.value?.toMutableList() ?: mutableListOf()
                val index = current.indexOfFirst { it.address == device.address }
                if (index >= 0) {
                    current[index] = device
                } else {
                    current.add(device)
                }
                _devices.value = current
            }
        }

        metricsJob = viewModelScope.launch {
            feature.metricsUpdate.collectLatest { metrics ->
                _currentSpeed.value = metrics.speed
                _metrics.value = mapMetrics(metrics)
            }
        }

        errorJob = viewModelScope.launch {
            feature.error.collectLatest { message ->
                _connectionStatus.value = message
                _isConnected.value = false
            }
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    fun startScan() {
        bleFeature?.startScan()
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    fun stopScan() {
        bleFeature?.stopScan()
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun connectToDevice(device: WheelDevice) {
        _connectionStatus.value = "Connecting to ${device.name}..."
        _isConnected.value = false
        bleFeature?.connectToDevice(device)
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun disconnect() {
        bleFeature?.disconnect()
    }

    private fun mapMetrics(metrics: WheelMetrics): List<VehicleMetric> {
        return buildList {
            add(VehicleMetric("SPEED", "%.1f".format(metrics.speed), "km/h"))
            add(VehicleMetric("BATTERY", metrics.batteryLevel.toString(), "%"))
            add(VehicleMetric("VOLTAGE", "%.1f".format(metrics.voltage), "V"))
            add(VehicleMetric("CURRENT", "%.1f".format(metrics.current), "A"))
            add(VehicleMetric("TEMP", "%.1f".format(metrics.temperature), "°C"))
            add(VehicleMetric("DISTANCE", "%.1f".format(metrics.distance), "km"))
        }
    }

    fun cleanup() {
        stateJob?.cancel()
        devicesJob?.cancel()
        metricsJob?.cancel()
        errorJob?.cancel()
        bleFeature?.cleanup()
        bleFeature = null
    }

    override fun onCleared() {
        super.onCleared()
        cleanup()
    }
}