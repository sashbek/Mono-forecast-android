package org.pakicek.monoforecast.data.features

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Looper
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.pakicek.monoforecast.domain.model.GnssState
import org.pakicek.monoforecast.domain.repository.LogsRepository
import org.pakicek.monoforecast.domain.repository.SettingsRepository

class GnssFeature(
    private val context: Context,
    private val settingsRepository: SettingsRepository,
    private val logsRepository: LogsRepository
) : BackgroundFeature<GnssState> {

    private val _state = MutableStateFlow(GnssState.STOPPED)
    override val state: StateFlow<GnssState> = _state.asStateFlow()

    private val client = LocationServices.getFusedLocationProviderClient(context)
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var lastLocationTime = 0L
    private var checkJob: Job? = null

    companion object {
        const val ACTION_LOCATION_UPDATE = "org.pakicek.monoforecast.LOCATION_UPDATE"
    }

    private val callback = object : LocationCallback() {
        override fun onLocationResult(res: LocationResult) {
            res.lastLocation?.let { location ->
                lastLocationTime = System.currentTimeMillis()
                _state.value = GnssState.RUNNING

                scope.launch {
                    logsRepository.saveLocationLog(location.latitude, location.longitude)
                }

                val intent = Intent(ACTION_LOCATION_UPDATE).apply {
                    setPackage(context.packageName)
                    putExtra("lat", location.latitude)
                    putExtra("lon", location.longitude)
                }
                context.sendBroadcast(intent)
            }
        }
    }

    @SuppressLint("MissingPermission")
    override suspend fun start() {
        settingsRepository.setTrackingState(true)
        if (settingsRepository.getTrackingStartTime() == 0L) {
            settingsRepository.setTrackingStartTime(System.currentTimeMillis())
        }

        val interval = settingsRepository.getGnssInterval().ms
        val req = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, interval)
            .setMinUpdateIntervalMillis(interval)
            .build()

        withContext(Dispatchers.Main) {
            client.requestLocationUpdates(req, callback, Looper.getMainLooper())
        }
        _state.value = GnssState.RUNNING
        startHealthCheck()
    }

    override suspend fun stop() {
        settingsRepository.setTrackingState(false)
        settingsRepository.setTrackingStartTime(0L)
        withContext(Dispatchers.Main) {
            client.removeLocationUpdates(callback)
        }
        checkJob?.cancel()
        _state.value = GnssState.STOPPED
    }

    private fun startHealthCheck() {
        checkJob?.cancel()
        checkJob = scope.launch {
            while (isActive) {
                delay(10000)
                if (_state.value != GnssState.STOPPED) {
                    val gap = System.currentTimeMillis() - lastLocationTime
                    if (lastLocationTime > 0 && gap > 20000) {
                        _state.value = GnssState.LOST
                    } else {
                        _state.value = GnssState.RUNNING
                    }
                }
            }
        }
    }
}