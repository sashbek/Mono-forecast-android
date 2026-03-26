package org.pakicek.monoforecast.data.features

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.pakicek.monoforecast.domain.model.BleState

class BleFeature : IBackgroundFeature<BleState> {
    private val _state = MutableStateFlow(BleState.DISCONNECTED)
    override val state: StateFlow<BleState> = _state.asStateFlow()

    override suspend fun start() {
        if (_state.value == BleState.DISCONNECTED) {
            _state.value = BleState.CONNECTING
            delay(2000)
            _state.value = BleState.CONNECTED
        }
    }

    override suspend fun stop() {
        _state.value = BleState.DISCONNECTED
    }
}