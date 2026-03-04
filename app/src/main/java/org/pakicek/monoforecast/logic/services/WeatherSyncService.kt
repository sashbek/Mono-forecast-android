package org.pakicek.monoforecast.logic.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.pakicek.monoforecast.domain.repositories.ForecastRepository

class WeatherSyncService : Service() {
    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)
    private val repository by lazy { ForecastRepository(applicationContext) }

    companion object {
        const val ACTION_WEATHER_UPDATED = "org.pakicek.monoforecast.WEATHER_UPDATED"
        private const val TAG = "WeatherSyncService"
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Service Started (ID: $startId). Fetching weather...")
        serviceScope.launch {
            try {
                val isUpdated = repository.fetchAndSaveNewWeather()

                if (isUpdated) {
                    Log.d(TAG, "Weather updated from API/Mock.")
                } else {
                    Log.d(TAG, "Cache is still valid. No update needed.")
                }

                val broadcastIntent = Intent(ACTION_WEATHER_UPDATED)
                broadcastIntent.setPackage(packageName)
                sendBroadcast(broadcastIntent)
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