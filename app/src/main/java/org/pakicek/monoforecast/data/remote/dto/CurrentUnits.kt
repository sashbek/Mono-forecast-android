package org.pakicek.monoforecast.data.remote.dto

import com.google.gson.annotations.SerializedName

data class CurrentUnits(
    @SerializedName("temperature_2m") val temperature: Double,
    @SerializedName("relative_humidity_2m") val humidity: Int,
    @SerializedName("wind_speed_10m") val windSpeed: Double,
    @SerializedName("wind_direction_10m") val windDirection: Int,
    @SerializedName("cloud_cover") val cloudCover: Int
)