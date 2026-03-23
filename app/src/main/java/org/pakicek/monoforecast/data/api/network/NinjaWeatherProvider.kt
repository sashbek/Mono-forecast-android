package org.pakicek.monoforecast.data.api.network

import org.pakicek.monoforecast.domain.model.NetworkResult
import org.pakicek.monoforecast.domain.model.dto.MainDto
import org.pakicek.monoforecast.domain.model.dto.WeatherResponseDto
import org.pakicek.monoforecast.domain.model.dto.WindDto

class NinjaWeatherProvider(private val apiKey: String) : WeatherProvider {

    override suspend fun fetchWeather(lat: Double, lon: Double): org.pakicek.monoforecast.domain.model.NetworkResult<org.pakicek.monoforecast.domain.model.dto.WeatherResponseDto> {
        val result = safeApiCall { RetrofitClients.ninjaApi.getWeather(lat, lon, apiKey) }

        return when (result) {
            is NetworkResult.Success -> {
                val ninjaDto = result.data
                val mapped = WeatherResponseDto(
                    main = MainDto(ninjaDto.temp, ninjaDto.humidity),
                    wind = WindDto(ninjaDto.windSpeed, ninjaDto.windDegrees),
                    cloudPct = ninjaDto.cloudPct,
                    timestamp = System.currentTimeMillis()
                )
                NetworkResult.Success(mapped)
            }
            is NetworkResult.Error -> {
                result
            }
        }
    }
}