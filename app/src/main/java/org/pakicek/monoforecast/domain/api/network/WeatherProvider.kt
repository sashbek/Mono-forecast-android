package org.pakicek.monoforecast.domain.api.network

import org.pakicek.monoforecast.domain.model.NetworkResult
import org.pakicek.monoforecast.domain.model.dto.WeatherResponseDto

interface WeatherProvider {
    suspend fun fetchWeather(lat: Double, lon: Double): NetworkResult<WeatherResponseDto>
}