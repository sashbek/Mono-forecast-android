package org.pakicek.monoforecast.domain.model

data class WeatherSnapshot(
    val tempC: Double,
    val windSpeedMs: Double,
    val rainMm: Double,
    val visibilityMeters: Int
)