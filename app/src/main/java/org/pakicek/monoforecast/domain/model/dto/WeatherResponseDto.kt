package org.pakicek.monoforecast.domain.model.dto

data class WeatherResponseDto(
    val main: MainDto,
    val wind: WindDto,
    val cloudPct: Int,
    val timestamp: Long
)