package org.pakicek.monoforecast.logic.factories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.pakicek.monoforecast.domain.model.dao.LogsDao
import org.pakicek.monoforecast.domain.repositories.LogsRepository
import org.pakicek.monoforecast.domain.repositories.SettingsRepository
import org.pakicek.monoforecast.logic.viewmodel.LogsViewModel

class LogsViewModelFactory(private val repository: LogsRepository, private val settingsRepository: SettingsRepository) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LogsViewModel::class.java)) {
            return LogsViewModel(repository, settingsRepository) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}