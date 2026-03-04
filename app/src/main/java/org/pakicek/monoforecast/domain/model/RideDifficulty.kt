package org.pakicek.monoforecast.domain.model

sealed interface RideDifficulty {
    data object Easy : RideDifficulty

    data class Moderate(val warnings: List<String>) : RideDifficulty

    data class Hard(val reason: String) : RideDifficulty

    data class Extreme(val reason: String) : RideDifficulty
}