package org.pakicek.monoforecast.data.remote.dto

import com.google.gson.annotations.SerializedName

data class NinjaWeatherDto(
    @SerializedName("temp") val temp: Double,
    @SerializedName("humidity") val humidity: Int,
    @SerializedName("wind_speed") val windSpeed: Double,
    @SerializedName("wind_degrees") val windDegrees: Int,
    @SerializedName("cloud_pct") val cloudPct: Int
)