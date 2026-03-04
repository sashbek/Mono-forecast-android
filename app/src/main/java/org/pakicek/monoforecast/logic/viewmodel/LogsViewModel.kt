package org.pakicek.monoforecast.logic.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import org.pakicek.monoforecast.domain.model.dto.logs.LogFrameEntity
import org.pakicek.monoforecast.domain.repositories.LogsRepository
import org.pakicek.monoforecast.domain.repositories.SettingsRepository

class LogsViewModel(
    private val repository: LogsRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _isLogging = MutableLiveData(false)
    val isLogging: LiveData<Boolean> = _isLogging

    init {
        checkLoggingStatus()
    }

    private fun checkLoggingStatus() {
        viewModelScope.launch {
            val active = repository.isLoggingActive()
            _isLogging.value = active
        }
    }

    fun toggleLogging() {
        viewModelScope.launch {
            val currentlyActive = repository.isLoggingActive()

            if (currentlyActive) {
                stopLogging()
            } else {
                startLogging()
            }
        }
    }

    private suspend fun startLogging() {
        repository.startNewFile()
        logCurrentSettings()
        _isLogging.value = true
    }

    private suspend fun stopLogging() {
        repository.endLastFile()
        _isLogging.value = false
    }

    private suspend fun logCurrentSettings() {
        val settings = settingsRepository.getAllSettings()
        settings.forEach {
            repository.insertSetting(it.setting, it.value)
        }
    }

    fun getLogs(): Flow<List<LogFrameEntity>> {
        return repository.getAllLogs()
    }
}
