package org.pakicek.monoforecast.presentation.logs

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.pakicek.monoforecast.data.repositories.LogsRepository

class LogsViewModelFactory(ctx: Context) : ViewModelProvider.Factory {
    private val repository: LogsRepository = LogsRepository(ctx)

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LogsViewModel::class.java)) {
            return LogsViewModel(repository) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}