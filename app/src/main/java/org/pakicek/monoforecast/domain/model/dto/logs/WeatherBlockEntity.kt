package org.pakicek.monoforecast.domain.model.dto.logs

data class WeatherBlockEntity(
    val tempC: Double,
    val visibilityMeters: Int,
    val windSpeedMs: Double,
    val windDir: Int,
    val rainMm: Double
)
