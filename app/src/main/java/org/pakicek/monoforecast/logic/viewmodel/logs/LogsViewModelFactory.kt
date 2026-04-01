package org.pakicek.monoforecast.logic.viewmodel.logs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.pakicek.monoforecast.domain.repository.LogsRepository

class LogsViewModelFactory(private val repository: LogsRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return LogsViewModel(repository) as T
    }
}