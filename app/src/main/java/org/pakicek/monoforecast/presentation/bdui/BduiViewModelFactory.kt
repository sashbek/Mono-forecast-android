package org.pakicek.monoforecast.presentation.bdui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.pakicek.monoforecast.domain.repository.BduiRepository

class BduiViewModelFactory(
    private val repo: BduiRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BduiViewModel::class.java)) {
            return BduiViewModel(repo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}