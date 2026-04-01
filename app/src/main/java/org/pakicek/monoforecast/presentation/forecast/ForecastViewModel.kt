package org.pakicek.monoforecast.presentation.forecast

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.pakicek.monoforecast.data.remote.dto.WeatherResponseDto
import org.pakicek.monoforecast.domain.model.RideDifficulty
import org.pakicek.monoforecast.domain.model.WeatherCondition
import org.pakicek.monoforecast.domain.repository.ForecastRepository
import org.pakicek.monoforecast.logic.analyzer.ForecastAnalyzer
import org.pakicek.monoforecast.logic.mapper.WeatherConditionMapper

class ForecastViewModel(private val repository: ForecastRepository) : ViewModel() {

    private val analyzer = ForecastAnalyzer()
    private val _weatherState = MutableLiveData<WeatherState>()
    val weatherState: LiveData<WeatherState> = _weatherState

    fun refreshData() {
        viewModelScope.launch {
            val weatherDto = repository.getLastKnownWeather()
            val difficulty = analyzer.analyzeDifficulty(weatherDto)
            val condition = WeatherConditionMapper.map(weatherDto)
            _weatherState.value = WeatherState(weatherDto, difficulty, condition)
        }
    }

    data class WeatherState(
        val weather: WeatherResponseDto,
        val difficulty: RideDifficulty,
        val condition: WeatherCondition
    )
}