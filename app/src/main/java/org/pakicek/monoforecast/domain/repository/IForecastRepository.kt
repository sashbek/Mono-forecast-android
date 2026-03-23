package org.pakicek.monoforecast.domain.repository

import org.pakicek.monoforecast.domain.model.dto.WeatherResponseDto

interface IForecastRepository {
    fun getLastKnownWeather(): WeatherResponseDto
}