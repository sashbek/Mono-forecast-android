package org.pakicek.monoforecast.domain.model

import org.pakicek.monoforecast.domain.BluetoothConnector
import org.pakicek.monoforecast.domain.exceptions.DeviceConnectionException
import org.pakicek.monoforecast.utils.toKmh

class EucDevice(
    name: String,
    private val batteryVoltage: Double,
    override val maxSpeedKm: Int
) : Vehicle(name), BluetoothConnector {

    class VoltageConfig {
        companion object {
            const val MAX_VOLTAGE_100V = 100.8
            const val CUTOFF_VOLTAGE = 68.0
        }
    }

    inner class Diagnostics {
        fun checkBatteryHealth(): String {
            return if (batteryVoltage > VoltageConfig.CUTOFF_VOLTAGE)
                "Battery OK for $name"
            else
                "Low Battery on $name"
        }
    }

    val isHighPerformance: Boolean
        get() = maxSpeedKm > 50 && batteryVoltage > 84.0

    override fun isSafeForRide(weather: WeatherSnapshot): Boolean {
        if (weather.tempC < 0) return false
        if (weather.windSpeedMs.toKmh() > 40) return false
        return true
    }

    override fun connect(address: String): Boolean {
        if (address.isBlank()) throw DeviceConnectionException("Empty address", 101)
        return true
    }

    override fun disconnect(): Boolean {
        return true
    }

    override fun getBatteryLevel(): Int {
        return ((batteryVoltage / VoltageConfig.MAX_VOLTAGE_100V) * 100).toInt()
    }
}