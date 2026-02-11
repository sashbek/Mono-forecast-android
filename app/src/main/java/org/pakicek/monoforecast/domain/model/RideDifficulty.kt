package org.pakicek.monoforecast.domain.model

sealed class RideDifficulty {
    object Easy : RideDifficulty()
    data class Moderate(val warnings: List<String>) : RideDifficulty()
    data class Hard(val dangerReason: String, val windSpeed: Double) : RideDifficulty()
    object Extreme : RideDifficulty()
}