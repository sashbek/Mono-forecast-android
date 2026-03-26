package org.pakicek.monoforecast.presentation.location

import android.annotation.SuppressLint
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.pakicek.monoforecast.domain.repository.ISettingsRepository
import java.util.concurrent.TimeUnit

class LocationViewModel(private val settings: ISettingsRepository) : ViewModel() {
    private val _isTracking = MutableLiveData(false)
    val isTracking: LiveData<Boolean> = _isTracking

    private val _timer = MutableLiveData("00:00:00")
    val timer: LiveData<String> = _timer

    private var job: Job? = null

    init {
        refreshState()
    }

    fun refreshState() {
        val active = settings.isTracking()
        if (_isTracking.value != active) {
            _isTracking.value = active
        }

        if (active) {
            startTimer()
        } else {
            stopTimer()
            _timer.value = "00:00:00"
        }
    }

    fun startTracking() {
        _isTracking.value = true
        settings.setTrackingStartTime(System.currentTimeMillis())
        startTimer()
    }

    fun stopTracking() {
        _isTracking.value = false
        stopTimer()
        _timer.value = "00:00:00"
    }

    @SuppressLint("DefaultLocale")
    private fun startTimer() {
        if (job?.isActive == true) return
        job = viewModelScope.launch {
            while (isActive) {
                val startTime = settings.getTrackingStartTime()
                val diff = if (startTime > 0) System.currentTimeMillis() - startTime else 0L

                _timer.postValue(String.format("%02d:%02d:%02d",
                    TimeUnit.MILLISECONDS.toHours(diff),
                    TimeUnit.MILLISECONDS.toMinutes(diff) % 60,
                    TimeUnit.MILLISECONDS.toSeconds(diff) % 60
                ))
                delay(1000)
            }
        }
    }

    private fun stopTimer() {
        job?.cancel()
        job = null
    }
}