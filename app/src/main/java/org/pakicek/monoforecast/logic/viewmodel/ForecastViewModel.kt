package org.pakicek.monoforecast.logic.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.pakicek.monoforecast.domain.model.RideDifficulty
import org.pakicek.monoforecast.domain.model.dto.WeatherResponseDto
import org.pakicek.monoforecast.domain.repositories.ForecastRepository
import org.pakicek.monoforecast.logic.ForecastAnalyzer

class ForecastViewModel(private val repository: ForecastRepository) : ViewModel() {
    private val analyzer = ForecastAnalyzer()

    // LiveData - это хранилище данных, за которым следит Activity
    private val _weatherState = MutableLiveData<WeatherState>()
    val weatherState: LiveData<WeatherState> = _weatherState

    init {
        refreshData()
    }

    fun refreshData() {
        val weatherDto = repository.getLastKnownWeather()
        val difficulty = analyzer.analyzeDifficulty(weatherDto)
        _weatherState.postValue(WeatherState(weatherDto, difficulty))
    }

    data class WeatherState(
        val weather: WeatherResponseDto,
        val difficulty: RideDifficulty
    )
}