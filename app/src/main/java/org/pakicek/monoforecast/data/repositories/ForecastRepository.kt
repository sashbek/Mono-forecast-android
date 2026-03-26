package org.pakicek.monoforecast.data.repositories

import android.content.Context
import androidx.core.content.edit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.pakicek.monoforecast.BuildConfig
import org.pakicek.monoforecast.data.api.OpenMeteoApiInterface
import org.pakicek.monoforecast.data.api.WeatherApiInterface
import org.pakicek.monoforecast.domain.model.NetworkResult
import org.pakicek.monoforecast.domain.model.dto.MainDto
import org.pakicek.monoforecast.domain.model.dto.WeatherResponseDto
import org.pakicek.monoforecast.domain.model.dto.WindDto
import org.pakicek.monoforecast.domain.model.dto.enums.WeatherApi
import org.pakicek.monoforecast.domain.repository.IForecastRepository
import org.pakicek.monoforecast.domain.repository.ISettingsRepository
import retrofit2.Response
import kotlin.random.Random

class ForecastRepository(
    context: Context,
    private val settingsRepo: ISettingsRepository,
    private val locationProvider: LocationProvider,
    private val ninjaApi: WeatherApiInterface,
    private val openMeteoApi: OpenMeteoApiInterface
) : IForecastRepository {

    private val prefs = context.getSharedPreferences("weather_cache", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_TEMP = "temp"
        private const val KEY_HUMIDITY = "humidity"
        private const val KEY_WIND_SPEED = "wind_speed"
        private const val KEY_WIND_DIRECTION = "wind_direction"
        private const val KEY_CLOUD_PCT = "cloud_pct"
        private const val KEY_HAS_DATA = "has_data"
        private const val KEY_LAST_UPDATE = "last_update_time"
        private const val FALLBACK_LAT = 51.5074
        private const val FALLBACK_LON = 0.1278
    }

    override fun getLastKnownWeather(): WeatherResponseDto {
        if (!prefs.contains(KEY_HAS_DATA)) {
            return generateEmptyWeather()
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
            timestamp = prefs.getLong(KEY_LAST_UPDATE, System.currentTimeMillis())
        )
    }

    override suspend fun fetchAndSaveNewWeather(): NetworkResult<Unit> = withContext(Dispatchers.IO) {
        if (isCacheValid()) {
            return@withContext NetworkResult.Success(Unit)
        }

        val coords = locationProvider.getCurrentLocation()
        val lat = coords?.lat ?: FALLBACK_LAT
        val lon = coords?.lon ?: FALLBACK_LON

        val result = when (settingsRepo.getApi()) {
            WeatherApi.NINJA_API -> fetchNinja(lat, lon)
            WeatherApi.OPEN_METEO -> fetchOpenMeteo(lat, lon)
            WeatherApi.MOCK -> NetworkResult.Success(generateMockDto())
        }

        if (result is NetworkResult.Success) {
            saveToCache(result.data)
            NetworkResult.Success(Unit)
        } else {
            val error = result as NetworkResult.Error
            NetworkResult.Error(error.code, error.message, error.exception)
        }
    }

    private suspend fun fetchNinja(lat: Double, lon: Double): NetworkResult<WeatherResponseDto> {
        return safeApiCall { ninjaApi.getWeather(lat, lon, BuildConfig.NINJA_API_KEY) }.map { dto ->
            WeatherResponseDto(
                main = MainDto(dto.temp, dto.humidity),
                wind = WindDto(dto.windSpeed, dto.windDegrees),
                cloudPct = dto.cloudPct,
                timestamp = System.currentTimeMillis()
            )
        }
    }

    private suspend fun fetchOpenMeteo(lat: Double, lon: Double): NetworkResult<WeatherResponseDto> {
        return safeApiCall { openMeteoApi.getWeather(lat, lon) }.map { dto ->
            val current = dto.current
            WeatherResponseDto(
                main = MainDto(current.temperature, current.humidity),
                wind = WindDto(current.windSpeed, current.windDirection),
                cloudPct = current.cloudCover,
                timestamp = System.currentTimeMillis()
            )
        }
    }

    private fun isCacheValid(): Boolean {
        val lastUpdate = prefs.getLong(KEY_LAST_UPDATE, 0)
        val currentTime = System.currentTimeMillis()
        val cacheDuration = settingsRepo.getCacheDuration().milliseconds
        if (cacheDuration == 0L) return false
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

    private fun generateMockDto() = WeatherResponseDto(
        main = MainDto(Random.nextDouble(-5.0, 35.0), Random.nextInt(20, 90)),
        wind = WindDto(Random.nextDouble(0.0, 20.0), Random.nextInt(0, 360)),
        cloudPct = Random.nextInt(0, 100),
        timestamp = System.currentTimeMillis()
    )

    private fun generateEmptyWeather() = WeatherResponseDto(
        main = MainDto(0.0, 0),
        wind = WindDto(0.0, 0),
        cloudPct = 0,
        timestamp = System.currentTimeMillis()
    )

    private suspend fun <T> safeApiCall(apiCall: suspend () -> Response<T>): NetworkResult<T> {
        return try {
            val response = apiCall()
            if (response.isSuccessful && response.body() != null) {
                NetworkResult.Success(response.body()!!)
            } else {
                NetworkResult.Error(response.code(), response.message())
            }
        } catch (e: Exception) {
            NetworkResult.Error(-1, e.message ?: "Unknown error", e)
        }
    }

    private fun <T, R> NetworkResult<T>.map(transform: (T) -> R): NetworkResult<R> {
        return when (this) {
            is NetworkResult.Success -> NetworkResult.Success(transform(data))
            is NetworkResult.Error -> NetworkResult.Error(code, message, exception)
        }
    }
}