package org.pakicek.monoforecast.domain.repositories

import android.content.Context
import androidx.core.content.edit
import org.pakicek.monoforecast.domain.model.dto.MainDto
import org.pakicek.monoforecast.domain.model.dto.WeatherResponseDto
import org.pakicek.monoforecast.domain.model.dto.WindDto
import kotlin.random.Random

class ForecastRepository(context: Context) {
    private val prefs = context.getSharedPreferences("weather_cache", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_TEMP = "temp"
        private const val KEY_WIND_SPEED = "wind_speed"
        private const val KEY_WIND_DIRECTION = "wind_direction"
        private const val KEY_VISIBILITY = "visibility"
        private const val KEY_RAIN = "rain"
        private const val KEY_HAS_DATA = "has_data"
    }


    fun getLastKnownWeather(): WeatherResponseDto {
        if (!prefs.contains(KEY_HAS_DATA)) {
            return WeatherResponseDto(
                main = MainDto(0.0, 0.0, 0, 0),
                wind = WindDto(0.0, 0),
                visibility = 10000,
                timestamp = System.currentTimeMillis()
            )
        }

        val temp = prefs.getFloat(KEY_TEMP, 0f).toDouble()
        val windSpeed = prefs.getFloat(KEY_WIND_SPEED, 0f).toDouble()
        val windDirection = prefs.getInt(KEY_WIND_DIRECTION, 0)
        val visibility = prefs.getInt(KEY_VISIBILITY, 10000)
        val rain = prefs.getFloat(KEY_RAIN, 0f).toDouble()

        return WeatherResponseDto(
            main = MainDto(temp = temp, rain = rain, humidity = 50, pressure = 1013),
            wind = WindDto(speed = windSpeed, direction = windDirection),
            visibility = visibility,
            timestamp = System.currentTimeMillis()
        )
    }

    fun fetchAndSaveNewWeather() {
        val mockDto = generateMockDto()
        saveToCache(mockDto)
    }

    private fun saveToCache(dto: WeatherResponseDto) {
        prefs.edit {
            putFloat(KEY_TEMP, dto.main.temp.toFloat())
            putFloat(KEY_WIND_SPEED, dto.wind.speed.toFloat())
            putInt(KEY_WIND_DIRECTION, dto.wind.direction)
            putInt(KEY_VISIBILITY, dto.visibility)
            putFloat(KEY_RAIN, dto.main.rain.toFloat())
            putBoolean(KEY_HAS_DATA, true)
        }
    }

    private fun generateMockDto(): WeatherResponseDto {
        return WeatherResponseDto(
            main = MainDto(
                temp = Random.nextDouble(5.0, 30.0),
                rain = if (Random.nextBoolean()) Random.nextDouble(0.0, 5.0) else 0.0,
                humidity = Random.nextInt(30, 90),
                pressure = 1013
            ),
            wind = WindDto(
                speed = Random.nextDouble(1.0, 15.0),
                direction = Random.nextInt(0, 360)
            ),
            visibility = if (Random.nextBoolean()) 10000 else 5000,
            timestamp = System.currentTimeMillis()
        )
    }
}