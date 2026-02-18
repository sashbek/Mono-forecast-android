package org.pakicek.monoforecast.logic.factories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.pakicek.monoforecast.domain.repositories.LogsRepository
import org.pakicek.monoforecast.logic.viewmodel.LogsViewModel

class LogsViewModelFactory(private val repository: LogsRepository) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T: ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LogsViewModel::class.java)) {
            return LogsViewModel(repository) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}