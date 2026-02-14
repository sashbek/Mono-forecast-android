package org.pakicek.monoforecast.domain.model.dto.logs

data class WeatherBlockEntity(
    val tempC: Double,
    val windSpeedMs: Double,
    val rainMm: Double,
    val visibilityMeters: Int
)
