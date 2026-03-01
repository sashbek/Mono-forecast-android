package org.pakicek.monoforecast.logic.factories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.pakicek.monoforecast.domain.repositories.ForecastRepository
import org.pakicek.monoforecast.logic.viewmodel.ForecastViewModel

class ForecastViewModelFactory(private val repository: ForecastRepository) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ForecastViewModel::class.java)) {
            return ForecastViewModel(repository) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}