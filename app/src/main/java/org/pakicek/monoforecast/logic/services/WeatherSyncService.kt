package org.pakicek.monoforecast.logic.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.pakicek.monoforecast.domain.repositories.ForecastRepository

class WeatherSyncService : Service() {
    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)

    // by lazy означает, что объект создастся только при первом обращении
    private val repository by lazy { ForecastRepository(applicationContext) }

    companion object {
        const val ACTION_WEATHER_UPDATED = "org.pakicek.monoforecast.WEATHER_UPDATED"
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("WeatherSyncService", "Service Started.")

        serviceScope.launch {
            try {
                repository.fetchAndSaveNewWeather()

                Log.d("WeatherSyncService", "Weather updated successfully.")
                val broadcastIntent = Intent(ACTION_WEATHER_UPDATED)
                broadcastIntent.setPackage(packageName)
                sendBroadcast(broadcastIntent)
            } catch (e: Exception) {
                Log.e("WeatherSyncService", "Error updating weather", e)
            } finally {
                stopSelf()
            }
        }

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceJob.cancel()
    }
}