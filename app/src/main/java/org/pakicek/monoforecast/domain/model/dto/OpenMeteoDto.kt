package org.pakicek.monoforecast.domain.model.dto

import com.google.gson.annotations.SerializedName

data class OpenMeteoDto(
    @SerializedName("current") val current: CurrentUnits
)