package org.pakicek.monoforecast.presentation.ble.connection.adapters

import org.pakicek.monoforecast.presentation.ble.connection.models.WheelMetrics

abstract class WheelProtocolAdapter {
    abstract fun parseData(data: ByteArray): WheelMetrics?
    abstract fun getAdapterType(): String
}