package org.pakicek.monoforecast.domain.repositories

import android.content.Context
import android.util.Log
import androidx.core.content.edit
import kotlinx.coroutines.delay
import org.pakicek.monoforecast.domain.api.RetrofitClient
import org.pakicek.monoforecast.domain.model.dto.MainDto
import org.pakicek.monoforecast.domain.model.dto.WeatherResponseDto
import org.pakicek.monoforecast.domain.model.dto.WindDto
import org.pakicek.monoforecast.domain.model.dto.enums.WeatherApi
import kotlin.random.Random

class ForecastRepository(context: Context) {
    private val prefs = context.getSharedPreferences("weather_cache", Context.MODE_PRIVATE)
    private val settingsPrefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_TEMP = "temp"
        private const val KEY_HUMIDITY = "humidity"
        private const val KEY_WIND_SPEED = "wind_speed"
        private const val KEY_WIND_DIRECTION = "wind_direction"
        private const val KEY_CLOUD_PCT = "cloud_pct"
        private const val KEY_HAS_DATA = "has_data"
        private const val KEY_API_SETTING = "KEY_API"
        private const val API_KEY = "mqccZREuuaHTZxfWv51DCSArwrekGpmoeOzQMN6A"
        private const val DEFAULT_LAT = 51.5074
        private const val DEFAULT_LON = 0.1278
    }

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

    suspend fun fetchAndSaveNewWeather() {
        val apiName = settingsPrefs.getString(KEY_API_SETTING, WeatherApi.MOCK.name)
        val selectedApi = try {
            WeatherApi.valueOf(apiName ?: WeatherApi.NINJA_API.name)
        } catch (e: Exception) {
            WeatherApi.MOCK
        }
        when (selectedApi) {
            WeatherApi.NINJA_API -> fetchFromNetwork()
            WeatherApi.MOCK -> fetchFromMock()
        }
    }

    private suspend fun fetchFromNetwork() {
        try {
            Log.d("ForecastRepo", "Fetching from NinjaAPI...")
            val response = RetrofitClient.api.getWeather(DEFAULT_LAT, DEFAULT_LON, API_KEY)

            if (response.isSuccessful && response.body() != null) {
                val ninjaDto = response.body()!!

                val appDto = WeatherResponseDto(
                    main = MainDto(
                        temp = ninjaDto.temp,
                        humidity = ninjaDto.humidity
                    ),
                    wind = WindDto(
                        speed = ninjaDto.windSpeed,
                        direction = ninjaDto.windDegrees
                    ),
                    cloudPct = ninjaDto.cloudPct,
                    timestamp = System.currentTimeMillis()
                )

                saveToCache(appDto)
                Log.d("ForecastRepo", "Success from Network: ${appDto.main.temp}C")
            } else {
                Log.e("ForecastRepo", "API Error: ${response.code()} ${response.message()}")
            }
        } catch (e: Exception) {
            Log.e("ForecastRepo", "Network Error", e)
        }
    }

    private suspend fun fetchFromMock() {
        Log.d("ForecastRepo", "Generating Mock Data...")
        delay(1000)
        val mockDto = generateMockDto()
        saveToCache(mockDto)
        Log.d("ForecastRepo", "Mock data saved: ${mockDto.main.temp}C")
    }

    private fun saveToCache(dto: WeatherResponseDto) {
        prefs.edit {
            putFloat(KEY_TEMP, dto.main.temp.toFloat())
            putInt(KEY_HUMIDITY, dto.main.humidity)
            putFloat(KEY_WIND_SPEED, dto.wind.speed.toFloat())
            putInt(KEY_WIND_DIRECTION, dto.wind.direction)
            putInt(KEY_CLOUD_PCT, dto.cloudPct)
            putBoolean(KEY_HAS_DATA, true)
        }
    }

    private fun generateMockDto(): WeatherResponseDto {
        return WeatherResponseDto(
            main = MainDto(
                temp = Random.nextDouble(5.0, 30.0),
                humidity = Random.nextInt(30, 90)
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