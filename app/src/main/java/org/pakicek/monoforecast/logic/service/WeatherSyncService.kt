package org.pakicek.monoforecast.logic.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.pakicek.monoforecast.MonoForecastApp
import org.pakicek.monoforecast.domain.model.NetworkResult

class WeatherSyncService : Service() {

    private val scope = CoroutineScope(Dispatchers.IO + Job())

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val repository = (application as MonoForecastApp).container.forecastRepository

        scope.launch {
            val result = repository.fetchAndSaveNewWeather()

            val broadcast = Intent(ACTION_WEATHER_UPDATED).apply {
                setPackage(packageName)
                putExtra(EXTRA_IS_SUCCESS, result is NetworkResult.Success)
                if (result is NetworkResult.Error) {
                    putExtra(EXTRA_ERROR_MESSAGE, result.message ?: "Unknown error")
                }
            }
            sendBroadcast(broadcast)
            stopSelf(startId)
        }

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val ACTION_WEATHER_UPDATED = "org.pakicek.monoforecast.WEATHER_UPDATED"
        const val EXTRA_IS_SUCCESS = "is_success"
        const val EXTRA_ERROR_MESSAGE = "error_message"
    }
}