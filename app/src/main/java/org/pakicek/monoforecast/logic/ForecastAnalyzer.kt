package org.pakicek.monoforecast.logic

import org.pakicek.monoforecast.domain.model.RideDifficulty
import org.pakicek.monoforecast.domain.model.Vehicle
import org.pakicek.monoforecast.domain.model.WeatherSnapshot
import org.pakicek.monoforecast.utils.toKmh

class ForecastAnalyzer {
    fun analyzeDifficulty(weather: WeatherSnapshot, vehicle: Vehicle): RideDifficulty {
        if (!vehicle.isSafeForRide(weather)) {
            return RideDifficulty.Hard("Vehicle limitation exceeded", weather.windSpeedMs)
        }

        return when {
            weather.rainMm > 5.0 -> RideDifficulty.Extreme
            weather.tempC < 5.0 -> RideDifficulty.Moderate(listOf("Ice risk", "Cold hands"))
            weather.windSpeedMs.toKmh() > 25 -> RideDifficulty.Moderate(listOf("Crosswind"))
            else -> RideDifficulty.Easy
        }
    }
}