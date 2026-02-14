package org.pakicek.monoforecast.domain

import org.pakicek.monoforecast.domain.model.WeatherSnapshot
import kotlin.random.Random

class WeatherRepository {
    fun getCurrentWeather(): WeatherSnapshot {
        val temp = Random.nextDouble(-5.0, 35.0)
        val wind = Random.nextDouble(0.0, 15.0)
        val rain = if (Random.nextBoolean()) Random.nextDouble(0.0, 10.0) else 0.0

        return WeatherSnapshot(
            tempC = (temp * 10).toInt() / 10.0,
            windSpeedMs = (wind * 10).toInt() / 10.0,
            rainMm = (rain * 10).toInt() / 10.0,
            visibilityMeters = 10000
        )
    }
}