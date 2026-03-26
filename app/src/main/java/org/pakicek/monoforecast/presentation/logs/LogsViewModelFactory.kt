package org.pakicek.monoforecast.presentation.logs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.pakicek.monoforecast.domain.repository.ILogsRepository

class LogsViewModelFactory(private val repository: ILogsRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return LogsViewModel(repository) as T
    }
}