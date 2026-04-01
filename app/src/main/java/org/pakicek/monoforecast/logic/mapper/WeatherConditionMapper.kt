package org.pakicek.monoforecast.logic.mapper

import org.pakicek.monoforecast.data.remote.dto.WeatherResponseDto
import org.pakicek.monoforecast.domain.model.WeatherCondition

object WeatherConditionMapper {
    fun map(weather: WeatherResponseDto): WeatherCondition {
        val temp = weather.main.temp
        val clouds = weather.cloudPct

        return when {
            temp <= 0 && clouds > 50 -> WeatherCondition.SNOW
            temp > 0 && clouds > 80 -> WeatherCondition.RAIN
            clouds > 20 -> WeatherCondition.CLOUDY
            else -> WeatherCondition.CLEAR
        }
    }
}