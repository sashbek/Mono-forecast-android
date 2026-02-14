package org.pakicek.monoforecast.domain.model

abstract class Vehicle(val name: String) {
    abstract val maxSpeedKm: Int

    abstract fun isSafeForRide(weather: WeatherSnapshot): Boolean

    open fun getDescription(): String {
        return "Vehicle: $name, Max Speed: $maxSpeedKm km/h"
    }
}