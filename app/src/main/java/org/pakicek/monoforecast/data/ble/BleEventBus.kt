package org.pakicek.monoforecast.data.ble

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import org.pakicek.monoforecast.domain.model.ble.WheelDevice
import org.pakicek.monoforecast.domain.model.ble.WheelMetrics

object BleEventBus {

    sealed class Event {
        data class DeviceFound(val device: WheelDevice) : Event()
        data class MetricsUpdate(val metrics: WheelMetrics) : Event()
        data object Connected : Event()
        data object Disconnected : Event()
        data object ScanStarted : Event()
        data object ScanStopped : Event()
        data class Error(val message: String) : Event()
    }

    private val _events = MutableSharedFlow<Event>(extraBufferCapacity = 64)
    val events: SharedFlow<Event> = _events.asSharedFlow()

    suspend fun emit(event: Event) {
        _events.emit(event)
    }

    fun tryEmit(event: Event) {
        _events.tryEmit(event)
    }
}