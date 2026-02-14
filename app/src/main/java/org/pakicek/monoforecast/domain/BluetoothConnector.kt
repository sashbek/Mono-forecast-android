package org.pakicek.monoforecast.domain

interface BluetoothConnector {
    fun connect(address: String): Boolean
    fun disconnect(): Boolean
    fun getBatteryLevel(): Int
}