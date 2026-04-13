package org.pakicek.monoforecast.presentation.sdui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.pakicek.monoforecast.domain.repository.EchoRepository

class SduiViewModelFactory(
    private val repo: EchoRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SduiViewModel::class.java)) {
            return SduiViewModel(repo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}