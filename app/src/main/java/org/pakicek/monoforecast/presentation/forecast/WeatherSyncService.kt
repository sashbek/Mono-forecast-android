package org.pakicek.monoforecast.presentation.forecast

import android.app.Service
import android.content.Intent
import android.os.IBinder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.pakicek.monoforecast.MonoForecastApp
import org.pakicek.monoforecast.domain.model.NetworkResult

class WeatherSyncService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    companion object {
        const val ACTION_WEATHER_UPDATED = "org.pakicek.monoforecast.WEATHER_UPDATED"
        const val EXTRA_IS_SUCCESS = "EXTRA_IS_SUCCESS"
        const val EXTRA_ERROR_MESSAGE = "EXTRA_ERROR_MESSAGE"
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val app = application as MonoForecastApp
        val repo = app.container.forecastRepository

        serviceScope.launch {
            val result = repo.fetchAndSaveNewWeather()
            broadcastResult(result)
            stopSelf()
        }

        return START_NOT_STICKY
    }

    private fun broadcastResult(result: NetworkResult<Unit>) {
        val intent = Intent(ACTION_WEATHER_UPDATED).apply {
            setPackage(packageName)
            when (result) {
                is NetworkResult.Success -> putExtra(EXTRA_IS_SUCCESS, true)
                is NetworkResult.Error -> {
                    putExtra(EXTRA_IS_SUCCESS, false)
                    putExtra(EXTRA_ERROR_MESSAGE, result.message)
                }
            }
        }
        sendBroadcast(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}