package org.pakicek.monoforecast.data.remote.dto

import com.google.gson.annotations.SerializedName

data class OpenMeteoDto(
    @SerializedName("current") val current: CurrentUnits
)