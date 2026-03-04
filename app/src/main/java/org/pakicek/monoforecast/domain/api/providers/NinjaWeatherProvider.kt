package org.pakicek.monoforecast.domain.api.providers

import android.util.Log
import org.pakicek.monoforecast.domain.api.RetrofitClients
import org.pakicek.monoforecast.domain.api.WeatherProvider
import org.pakicek.monoforecast.domain.model.dto.MainDto
import org.pakicek.monoforecast.domain.model.dto.WeatherResponseDto
import org.pakicek.monoforecast.domain.model.dto.WindDto

class NinjaWeatherProvider(private val apiKey: String) : WeatherProvider {
    override suspend fun fetchWeather(lat: Double, lon: Double): WeatherResponseDto? {
        try {
            val response = RetrofitClients.ninjaApi.getWeather(lat, lon, apiKey)
            if (response.isSuccessful && response.body() != null) {
                val ninjaDto = response.body()!!

                return WeatherResponseDto(
                    main = MainDto(ninjaDto.temp, ninjaDto.humidity),
                    wind = WindDto(ninjaDto.windSpeed, ninjaDto.windDegrees),
                    cloudPct = ninjaDto.cloudPct,
                    timestamp = System.currentTimeMillis()
                )
            } else {
                Log.e("NinjaProvider", "Error: ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e("NinjaProvider", "Exception", e)
        }
        return null
    }
}