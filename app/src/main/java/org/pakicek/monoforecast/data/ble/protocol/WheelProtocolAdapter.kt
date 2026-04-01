package org.pakicek.monoforecast.data.ble.protocol

import org.pakicek.monoforecast.domain.model.ble.WheelMetrics

abstract class WheelProtocolAdapter {
    abstract fun parseData(data: ByteArray): WheelMetrics?
    abstract fun getAdapterType(): String
}