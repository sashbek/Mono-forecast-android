package org.pakicek.monoforecast.domain.api

import org.pakicek.monoforecast.domain.model.dto.NinjaWeatherDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface WeatherApiInterface {
    // Документация: https://api-ninjas.com/api/weather
    @GET("v1/weather")
    suspend fun getWeather(
        @Query("city") city: String,
        @Header("X-Api-Key") apiKey: String
    ): Response<NinjaWeatherDto>
}