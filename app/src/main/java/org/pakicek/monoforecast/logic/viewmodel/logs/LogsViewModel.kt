package org.pakicek.monoforecast.logic.viewmodel.logs

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.mikephil.charting.data.Entry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.pakicek.monoforecast.data.local.entity.FileEntity
import org.pakicek.monoforecast.data.local.entity.LogWithDetails
import org.pakicek.monoforecast.domain.model.settings.LogType
import org.pakicek.monoforecast.domain.repository.LogsRepository

class LogsViewModel(private val repository: LogsRepository) : ViewModel() {

    private val _isLogging = MutableLiveData(false)
    val isLogging: LiveData<Boolean> = _isLogging

    private val _chartData = MutableStateFlow<List<Entry>>(emptyList())
    val chartData: StateFlow<List<Entry>> = _chartData.asStateFlow()

    init {
        viewModelScope.launch { _isLogging.value = repository.isLoggingActive() }
    }

    fun toggleLogging() {
        viewModelScope.launch {
            if (repository.isLoggingActive()) {
                repository.endLastFile()
                _isLogging.value = false
            } else {
                repository.startNewFile()
                _isLogging.value = true
            }
        }
    }

    fun getAllFiles(): Flow<List<FileEntity>> = repository.getAllFiles()

    suspend fun getLogsForFile(fileId: Long): Flow<List<LogWithDetails>> {
        val flow = repository.getLogsForSession(fileId)
        viewModelScope.launch {
            flow.collect { processChartData(it) }
        }
        return flow
    }

    private fun processChartData(logs: List<LogWithDetails>) {
        viewModelScope.launch(Dispatchers.Default) {
            val weatherLogs = logs.filter { it.log.type == LogType.WEATHER && it.weather != null }
            if (weatherLogs.isEmpty()) {
                _chartData.value = emptyList()
                return@launch
            }

            val startTime = weatherLogs.first().log.timestamp
            val entries = weatherLogs.map {
                Entry((it.log.timestamp - startTime) / 1000f, it.weather!!.tempC.toFloat())
            }
            _chartData.value = entries
        }
    }

    fun clearLogs() {
        viewModelScope.launch {
            if (_isLogging.value == true) toggleLogging()
            repository.clearAll()
        }
    }
}