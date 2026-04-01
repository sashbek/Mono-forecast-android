package org.pakicek.monoforecast.domain.repository

import org.pakicek.monoforecast.data.remote.dto.WeatherResponseDto
import org.pakicek.monoforecast.domain.model.NetworkResult

interface ForecastRepository {
    fun getLastKnownWeather(): WeatherResponseDto
    suspend fun fetchAndSaveNewWeather(): NetworkResult<Unit>
}