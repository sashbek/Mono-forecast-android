package org.pakicek.monoforecast.logic.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.pakicek.monoforecast.domain.repositories.LogsRepository
import org.pakicek.monoforecast.domain.repositories.SettingsRepository

class LogsViewModel (
    private val repository: LogsRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {
    private val _isLogging = MutableStateFlow(false)
    val isLogging = _isLogging.asStateFlow()

    init {
        viewModelScope.launch {
            _isLogging.value = repository.isLoggingActive()
        }
    }

    fun toggleLogging() {
        if (isLogging.value) {
            viewModelScope.launch {
                repository.endLastFile()
                _isLogging.value = false
            }
        } else {
            viewModelScope.launch {
                repository.startNewFile()
                logSettings()
                _isLogging.value = true
            }
        }
    }

    private suspend fun logSettings() {
        val settings = settingsRepository.getAllSettings()
        settings.forEach {
            repository.insertSetting(it.setting, it.value)
        }
    }
}
