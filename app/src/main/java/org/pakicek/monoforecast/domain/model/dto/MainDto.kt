package org.pakicek.monoforecast.domain.model.dto

data class MainDto(
    val temp: Double,
    val rain: Double,
    val humidity: Int,
    val pressure: Int
)