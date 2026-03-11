package org.pakicek.monoforecast.domain.repositories

import android.content.Context
import android.util.Log
import androidx.core.content.edit
import org.pakicek.monoforecast.domain.api.providers.NinjaWeatherProvider
import org.pakicek.monoforecast.domain.api.providers.OpenMeteoWeatherProvider
import org.pakicek.monoforecast.domain.model.NetworkResult
import org.pakicek.monoforecast.domain.model.dto.MainDto
import org.pakicek.monoforecast.domain.model.dto.WeatherResponseDto
import org.pakicek.monoforecast.domain.model.dto.WindDto
import org.pakicek.monoforecast.domain.model.dto.enums.WeatherApi
import kotlin.random.Random

class ForecastRepository(context: Context) {
    private val prefs = context.getSharedPreferences("weather_cache", Context.MODE_PRIVATE)
    private val settingsRepo = SettingsRepository(context)

    companion object {
        private const val KEY_TEMP = "temp"
        private const val KEY_HUMIDITY = "humidity"
        private const val KEY_WIND_SPEED = "wind_speed"
        private const val KEY_WIND_DIRECTION = "wind_direction"
        private const val KEY_CLOUD_PCT = "cloud_pct"
        private const val KEY_HAS_DATA = "has_data"
        private const val KEY_LAST_UPDATE = "last_update_time"
        private const val API_KEY_NINJA = "mqccZREuuaHTZxfWv51DCSArwrekGpmoeOzQMN6A"
        private const val DEFAULT_LAT = 51.5074
        private const val DEFAULT_LON = 0.1278
    }

    private val ninjaProvider = NinjaWeatherProvider(API_KEY_NINJA)
    private val openMeteoProvider = OpenMeteoWeatherProvider()

    fun getLastKnownWeather(): WeatherResponseDto {
        if (!prefs.contains(KEY_HAS_DATA)) {
            return WeatherResponseDto(
                main = MainDto(0.0, 0),
                wind = WindDto(0.0, 0),
                cloudPct = 0,
                timestamp = System.currentTimeMillis()
            )
        }

        return WeatherResponseDto(
            main = MainDto(
                temp = prefs.getFloat(KEY_TEMP, 0f).toDouble(),
                humidity = prefs.getInt(KEY_HUMIDITY, 50)
            ),
            wind = WindDto(
                speed = prefs.getFloat(KEY_WIND_SPEED, 0f).toDouble(),
                direction = prefs.getInt(KEY_WIND_DIRECTION, 0)
            ),
            cloudPct = prefs.getInt(KEY_CLOUD_PCT, 0),
            timestamp = System.currentTimeMillis()
        )
    }

    suspend fun fetchAndSaveNewWeather(): NetworkResult<Unit> {
        if (isCacheValid()) {
            return NetworkResult.Success(Unit)
        }

        val selectedApi = settingsRepo.getApi()

        val result = when (selectedApi) {
            WeatherApi.NINJA_API -> ninjaProvider.fetchWeather(DEFAULT_LAT, DEFAULT_LON)
            WeatherApi.OPEN_METEO -> openMeteoProvider.fetchWeather(DEFAULT_LAT, DEFAULT_LON)
            WeatherApi.MOCK -> NetworkResult.Success(generateMockDto())
        }

        return when (result) {
            is NetworkResult.Success -> {
                saveToCache(result.data)
                NetworkResult.Success(Unit)
            }
            is NetworkResult.Error -> {
                Log.e("ForecastRepo", "Update failed: ${result.message}")
                NetworkResult.Error(result.code, result.message, result.exception)
            }
        }
    }

    private fun isCacheValid(): Boolean {
        val lastUpdate = prefs.getLong(KEY_LAST_UPDATE, 0)
        val currentTime = System.currentTimeMillis()
        val cacheDuration = settingsRepo.getCacheDuration().milliseconds
        return (currentTime - lastUpdate) < cacheDuration
    }

    private fun saveToCache(dto: WeatherResponseDto) {
        prefs.edit {
            putFloat(KEY_TEMP, dto.main.temp.toFloat())
            putInt(KEY_HUMIDITY, dto.main.humidity)
            putFloat(KEY_WIND_SPEED, dto.wind.speed.toFloat())
            putInt(KEY_WIND_DIRECTION, dto.wind.direction)
            putInt(KEY_CLOUD_PCT, dto.cloudPct)
            putLong(KEY_LAST_UPDATE, System.currentTimeMillis())
            putBoolean(KEY_HAS_DATA, true)
        }
    }

    private fun generateMockDto(): WeatherResponseDto {
        return WeatherResponseDto(
            main = MainDto(
                temp = Random.nextDouble(-10.0, 30.0),
                humidity = Random.nextInt(10, 90)
            ),
            wind = WindDto(
                speed = Random.nextDouble(1.0, 15.0),
                direction = Random.nextInt(0, 360)
            ),
            cloudPct = Random.nextInt(0, 100),
            timestamp = System.currentTimeMillis()
        )
    }
}