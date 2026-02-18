package org.pakicek.monoforecast.logic.factories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.pakicek.monoforecast.domain.model.dao.LogsDao
import org.pakicek.monoforecast.logic.viewmodel.LogsViewModel

class LogsViewModelFactory(private val repository: LogsDao) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T: ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LogsViewModel::class.java)) {
            return LogsViewModel(repository) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}