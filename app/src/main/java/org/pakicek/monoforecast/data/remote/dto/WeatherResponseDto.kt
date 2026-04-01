package org.pakicek.monoforecast.data.remote.dto

data class WeatherResponseDto(
    val main: MainDto,
    val wind: WindDto,
    val cloudPct: Int,
    val timestamp: Long
)