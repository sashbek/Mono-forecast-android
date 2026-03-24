package org.pakicek.monoforecast.presentation.logs

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import org.pakicek.monoforecast.data.entities.FileEntity
import org.pakicek.monoforecast.data.entities.LogWithDetails
import org.pakicek.monoforecast.domain.repository.ILogsRepository

class LogsViewModel(
    private val repository: ILogsRepository
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
        _isLogging.value = true
    }

    private suspend fun stopLogging() {
        repository.endLastFile()
        _isLogging.value = false
    }

    fun getAllFiles(): Flow<List<FileEntity>> {
        return repository.getAllFiles()
    }

    fun getLogsForFile(fileId: Long): Flow<List<LogWithDetails>> = flow {
        val logsFlow = repository.getLogsForSession(fileId)
        emitAll(logsFlow)
    }

    fun clearLogs() {
        viewModelScope.launch {
            if (_isLogging.value == true) toggleLogging()
            repository.clearAll()
        }
    }
}