package org.pakicek.monoforecast.domain.api.providers

import android.util.Log
import org.pakicek.monoforecast.domain.api.RetrofitClients
import org.pakicek.monoforecast.domain.api.WeatherProvider
import org.pakicek.monoforecast.domain.model.dto.MainDto
import org.pakicek.monoforecast.domain.model.dto.WeatherResponseDto
import org.pakicek.monoforecast.domain.model.dto.WindDto

class OpenMeteoWeatherProvider : WeatherProvider {
    override suspend fun fetchWeather(lat: Double, lon: Double): WeatherResponseDto? {
        try {
            val response = RetrofitClients.openMeteoApi.getWeather(lat, lon)
            if (response.isSuccessful && response.body() != null) {
                val current = response.body()!!.current

                return WeatherResponseDto(
                    main = MainDto(current.temperature, current.humidity),
                    wind = WindDto(current.windSpeed, current.windDirection),
                    cloudPct = current.cloudCover,
                    timestamp = System.currentTimeMillis()
                )
            } else {
                Log.e("OpenMeteoProvider", "Error: ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e("OpenMeteoProvider", "Exception", e)
        }
        return null
    }
}