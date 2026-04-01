package org.pakicek.monoforecast.presentation.location

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.pakicek.monoforecast.domain.repository.SettingsRepository

class LocationViewModelFactory(private val settings: SettingsRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return LocationViewModel(settings) as T
    }
}