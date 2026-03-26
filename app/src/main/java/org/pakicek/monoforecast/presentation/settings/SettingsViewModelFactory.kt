package org.pakicek.monoforecast.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.pakicek.monoforecast.domain.repository.ILogsRepository
import org.pakicek.monoforecast.domain.repository.ISettingsRepository

class SettingsViewModelFactory(
    private val settingsRepository: ISettingsRepository,
    private val logsRepository: ILogsRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            return SettingsViewModel(settingsRepository, logsRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}