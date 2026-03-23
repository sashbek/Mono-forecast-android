package org.pakicek.monoforecast.presentation.forecast

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.pakicek.monoforecast.data.repositories.ForecastRepository

class ForecastViewModelFactory(ctx: Context) : ViewModelProvider.Factory {
    private val repository: ForecastRepository = ForecastRepository(ctx)
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ForecastViewModel::class.java)) {
            return ForecastViewModel(repository) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}