package org.pakicek.monoforecast.presentation.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.pakicek.monoforecast.data.repositories.LogsRepository
import org.pakicek.monoforecast.data.repositories.SettingsRepository

class SettingsViewModelFactory(ctx: Context) : ViewModelProvider.Factory {
    private val settingsRepository: SettingsRepository = SettingsRepository(ctx)
    private val logsRepository: LogsRepository = LogsRepository(ctx)

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            return SettingsViewModel(settingsRepository, logsRepository) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}