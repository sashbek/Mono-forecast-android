package org.pakicek.monoforecast.data.features.ble.adapters

import org.pakicek.monoforecast.domain.model.dto.ble.WheelMetrics

abstract class WheelProtocolAdapter {
    abstract fun parseData(data: ByteArray): WheelMetrics?
    abstract fun getAdapterType(): String
}