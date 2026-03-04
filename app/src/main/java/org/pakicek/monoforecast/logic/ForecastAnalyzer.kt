package org.pakicek.monoforecast.logic

import org.pakicek.monoforecast.domain.model.RideDifficulty
import org.pakicek.monoforecast.domain.model.dto.WeatherResponseDto

class ForecastAnalyzer {
    fun analyzeDifficulty(weather: WeatherResponseDto): RideDifficulty {
        return when {
            weather.main.temp < 0 -> RideDifficulty.Extreme
            weather.main.temp < 5 -> RideDifficulty.Moderate(listOf("Ice risk", "Cold"))
            weather.wind.speed > 25 -> RideDifficulty.Extreme
            weather.wind.speed > 12 -> RideDifficulty.Moderate(listOf("Crosswind"))
            else -> RideDifficulty.Easy
        }
    }
}