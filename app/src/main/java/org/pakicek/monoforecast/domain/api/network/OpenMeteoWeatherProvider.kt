package org.pakicek.monoforecast.domain.api.network

import org.pakicek.monoforecast.domain.model.NetworkResult
import org.pakicek.monoforecast.domain.model.dto.MainDto
import org.pakicek.monoforecast.domain.model.dto.WeatherResponseDto
import org.pakicek.monoforecast.domain.model.dto.WindDto

class OpenMeteoWeatherProvider : WeatherProvider {

    override suspend fun fetchWeather(lat: Double, lon: Double): NetworkResult<WeatherResponseDto> {
        val result = safeApiCall { RetrofitClients.openMeteoApi.getWeather(lat, lon) }

        return when (result) {
            is NetworkResult.Success -> {
                val current = result.data.current
                val mapped = WeatherResponseDto(
                    main = MainDto(current.temperature, current.humidity),
                    wind = WindDto(current.windSpeed, current.windDirection),
                    cloudPct = current.cloudCover,
                    timestamp = System.currentTimeMillis()
                )
                NetworkResult.Success(mapped)
            }
            is NetworkResult.Error -> result
        }
    }
}