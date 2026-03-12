package org.pakicek.monoforecast.logic.viewmodel

import android.annotation.SuppressLint
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.pakicek.monoforecast.domain.model.dto.VehicleMetric
import kotlin.random.Random

class BluetoothViewModel : ViewModel() {

    private val _currentSpeed = MutableLiveData(0f)
    val currentSpeed: LiveData<Float> = _currentSpeed

    private val _metrics = MutableLiveData<List<VehicleMetric>>()
    val metrics: LiveData<List<VehicleMetric>> = _metrics

    private val _connectionStatus = MutableLiveData("Disconnected")
    val connectionStatus: LiveData<String> = _connectionStatus

    @SuppressLint("DefaultLocale")
    fun startSimulation() {
        viewModelScope.launch {
            _connectionStatus.value = "Connecting..."
            delay(1000)
            _connectionStatus.value = "Connected: Begode Master"

            var speed = 0f
            var voltage = 100.8
            var distance = 1250.0

            while (isActive) {
                val delta = Random.nextFloat() * 2f - 1f
                speed = (speed + delta).coerceIn(0f, 55f)

                voltage = (voltage - 0.01).coerceAtLeast(65.0)
                distance += (speed / 3600 / 10)

                _currentSpeed.value = speed

                _metrics.value = listOf(
                    VehicleMetric("VOLTAGE", String.format("%.1f", voltage), "V"),
                    VehicleMetric("CURRENT", String.format("%.1f", speed * 1.5), "A"),
                    VehicleMetric("DISTANCE", String.format("%.1f", distance), "km"),
                    VehicleMetric("TEMP", "${Random.nextInt(35, 45)}", "°C")
                )

                delay(100)
            }
        }
    }
}