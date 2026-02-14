package org.pakicek.monoforecast.logic

import org.pakicek.monoforecast.domain.model.RideDifficulty
import org.pakicek.monoforecast.domain.model.dto.logs.WeatherBlockEntity
import org.pakicek.monoforecast.utils.toKmh

class ForecastAnalyzer {
    fun analyzeDifficulty(weather: WeatherBlockEntity): RideDifficulty {
        //if (!vehicle.isSafeForRide(weather)) {
        //    return RideDifficulty.Hard("Vehicle limitation exceeded", weather.windSpeedMs)
        //}

        return when {
            weather.rainMm > 5.0 -> RideDifficulty.Extreme
            weather.tempC < 5.0 -> RideDifficulty.Moderate(listOf("Ice risk", "Cold hands"))
            weather.windSpeedMs.toKmh() > 25 -> RideDifficulty.Moderate(listOf("Crosswind"))
            else -> RideDifficulty.Easy
        }
    }
}