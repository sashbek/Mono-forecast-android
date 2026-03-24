package org.pakicek.monoforecast.data.api

import org.pakicek.monoforecast.domain.model.dto.NinjaWeatherDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface WeatherApiInterface {
    // Docs: https://api-ninjas.com/api/weather
    @GET("v1/weather")
    suspend fun getWeather(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Header("X-Api-Key") apiKey: String
    ): Response<NinjaWeatherDto>
}