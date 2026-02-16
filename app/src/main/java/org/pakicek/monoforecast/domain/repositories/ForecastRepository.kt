package org.pakicek.monoforecast.domain.repositories

import org.pakicek.monoforecast.domain.model.dto.logs.WeatherBlockEntity
import kotlin.random.Random

class ForecastRepository {
    fun getCurrentWeather(): WeatherBlockEntity {
        val temp = Random.nextDouble(-5.0, 35.0)
        val wind = Random.nextDouble(0.0, 15.0)
        val rain = if (Random.nextBoolean()) Random.nextDouble(0.0, 10.0) else 0.0

        return WeatherBlockEntity(
            tempC = (temp * 10).toInt() / 10.0,
            windSpeedMs = (wind * 10).toInt() / 10.0,
            rainMm = (rain * 10).toInt() / 10.0,
            visibilityMeters = 10000
        )
    }
}