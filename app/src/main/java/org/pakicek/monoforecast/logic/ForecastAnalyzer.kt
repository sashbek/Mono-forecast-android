package org.pakicek.monoforecast.logic

import org.pakicek.monoforecast.domain.model.RideDifficulty
import org.pakicek.monoforecast.domain.model.dto.WeatherResponseDto

class ForecastAnalyzer {

    companion object {
        private const val TEMP_FREEZE = 0.0
        private const val TEMP_COLD = 5.0
        private const val WIND_GALE_MS = 25.0
        private const val WIND_STRONG_MS = 12.0
    }

    fun analyzeDifficulty(weather: WeatherResponseDto): RideDifficulty {
        return when {
            weather.main.temp < TEMP_FREEZE -> RideDifficulty.Extreme("Ice / Freezing conditions")
            weather.wind.speed > WIND_GALE_MS -> RideDifficulty.Extreme("Hurricane force winds")

            weather.main.temp < TEMP_COLD -> RideDifficulty.Moderate(listOf("Cold temperature", "Risk of ice"))
            weather.wind.speed > WIND_STRONG_MS -> RideDifficulty.Moderate(listOf("Strong Crosswind"))

            else -> RideDifficulty.Easy
        }
    }
}