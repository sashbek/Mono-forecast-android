package org.pakicek.monoforecast.data.remote.api

import org.pakicek.monoforecast.data.remote.dto.OpenMeteoDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface OpenMeteoApiInterface {
    @GET("v1/forecast")
    suspend fun getWeather(
        @Query("latitude") lat: Double,
        @Query("longitude") lon: Double,
        @Query("current") current: String = "temperature_2m,relative_humidity_2m,wind_speed_10m,wind_direction_10m,cloud_cover",
        @Query("wind_speed_unit") windUnit: String = "ms"
    ): Response<OpenMeteoDto>
}