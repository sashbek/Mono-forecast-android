package org.pakicek.monoforecast.presentation.forecast

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.pakicek.monoforecast.domain.model.NetworkResult
import org.pakicek.monoforecast.data.repositories.ForecastRepository
import org.pakicek.monoforecast.domain.repository.IForecastRepository

class WeatherSyncService : Service() {
    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)
    private val repository : IForecastRepository by lazy { ForecastRepository(applicationContext) }

    companion object {
        const val ACTION_WEATHER_UPDATED = "org.pakicek.monoforecast.WEATHER_UPDATED"
        const val EXTRA_IS_SUCCESS = "is_success"
        const val EXTRA_ERROR_MESSAGE = "error_message"
        private const val TAG = "WeatherSyncService"
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Service Started (ID: $startId). Fetching weather...")
        serviceScope.launch {
            try {
                val result = repository.fetchAndSaveNewWeather()
                val intent = Intent(ACTION_WEATHER_UPDATED).apply {
                    setPackage(packageName)
                    when (result) {
                        is NetworkResult.Success -> {
                            putExtra(EXTRA_IS_SUCCESS, true)
                            Log.d(TAG, "Success")
                        }
                        is NetworkResult.Error -> {
                            putExtra(EXTRA_IS_SUCCESS, false)
                            putExtra(EXTRA_ERROR_MESSAGE, result.message ?: "Unknown error")
                            Log.e(TAG, "Failed: ${result.message}")
                        }
                    }
                }
                sendBroadcast(intent)
            } catch (e: Exception) {
                Log.e(TAG, "Error updating weather: ${e.message}", e)
            } finally {
                stopSelf(startId)
            }
        }

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        Log.d(TAG, "Service Destroyed")
    }
}