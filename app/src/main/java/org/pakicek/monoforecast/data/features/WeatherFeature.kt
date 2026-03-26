package org.pakicek.monoforecast.data.features

import android.content.Context
import android.content.Intent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.pakicek.monoforecast.domain.model.NetworkResult
import org.pakicek.monoforecast.domain.model.dto.WeatherResponseDto
import org.pakicek.monoforecast.domain.repository.IForecastRepository

class WeatherFeature(
    private val context: Context,
    private val repository: IForecastRepository
) : IBackgroundFeature<WeatherResponseDto?> {

    private val _state = MutableStateFlow<WeatherResponseDto?>(null)
    override val state: StateFlow<WeatherResponseDto?> = _state.asStateFlow()

    companion object {
        const val ACTION_WEATHER_UPDATED = "org.pakicek.monoforecast.WEATHER_UPDATED"
    }

    override suspend fun start() {
        update()
    }

    suspend fun update() {
        val result = repository.fetchAndSaveNewWeather()
        _state.value = repository.getLastKnownWeather()

        val intent = Intent(ACTION_WEATHER_UPDATED).apply {
            setPackage(context.packageName)
            putExtra("is_success", result is NetworkResult.Success)
        }
        context.sendBroadcast(intent)
    }

    override suspend fun stop() {}

    suspend fun loadCache() {
        _state.value = repository.getLastKnownWeather()
    }
}