package org.pakicek.monoforecast.domain.model.dto

import com.google.gson.annotations.SerializedName

data class OpenMeteoDto(
    @SerializedName("current") val current: CurrentUnits
)

data class CurrentUnits(
    @SerializedName("temperature_2m") val temperature: Double,
    @SerializedName("relative_humidity_2m") val humidity: Int,
    @SerializedName("wind_speed_10m") val windSpeed: Double,
    @SerializedName("wind_direction_10m") val windDirection: Int,
    @SerializedName("cloud_cover") val cloudCover: Int
)