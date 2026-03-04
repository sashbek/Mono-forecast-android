package org.pakicek.monoforecast.domain.api

import org.pakicek.monoforecast.domain.model.dto.WeatherResponseDto

interface WeatherProvider {
    suspend fun fetchWeather(lat: Double, lon: Double): WeatherResponseDto?
}