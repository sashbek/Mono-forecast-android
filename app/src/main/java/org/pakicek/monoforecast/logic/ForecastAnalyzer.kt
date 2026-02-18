package org.pakicek.monoforecast.logic

import org.pakicek.monoforecast.domain.model.RideDifficulty
import org.pakicek.monoforecast.domain.model.dto.WeatherResponseDto

class ForecastAnalyzer {
    fun analyzeDifficulty(weather: WeatherResponseDto): RideDifficulty {
        return when {
            weather.main.rain > 5.0 -> RideDifficulty.Extreme
            weather.main.temp < 5.0 -> RideDifficulty.Moderate(listOf("Ice risk"))
            weather.wind.speed > 40 -> RideDifficulty.Moderate(listOf("Crosswind"))
            else -> RideDifficulty.Easy
        }
    }
}