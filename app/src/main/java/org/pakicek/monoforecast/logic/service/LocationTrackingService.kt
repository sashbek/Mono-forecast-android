package org.pakicek.monoforecast.logic.service

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import android.os.Looper
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.pakicek.monoforecast.R
import org.pakicek.monoforecast.data.repository.SettingsRepositoryImpl

class LocationTrackingService : Service() {

    private val scope = CoroutineScope(Dispatchers.IO + Job())

    private val fusedLocationClient by lazy {
        LocationServices.getFusedLocationProviderClient(this)
    }

    private val settingsRepository by lazy {
        SettingsRepositoryImpl(applicationContext)
    }

    private val container by lazy {
        (application as org.pakicek.monoforecast.MonoForecastApp).container
    }

    private val locationCallback = object : com.google.android.gms.location.LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            result.locations.forEach { location ->
                scope.launch {
                    container.logsRepository.saveLocationLog(location.latitude, location.longitude)
                }

                val intent = Intent(ACTION_LOCATION_UPDATE).apply {
                    setPackage(packageName)
                    putExtra(EXTRA_LAT, location.latitude)
                    putExtra(EXTRA_LON, location.longitude)
                }
                sendBroadcast(intent)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> startTracking()
            ACTION_STOP -> stopTracking()
        }
        return START_NOT_STICKY
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun startTracking() {
        settingsRepository.setTrackingState(true)
        if (settingsRepository.getTrackingStartTime() == 0L) {
            settingsRepository.setTrackingStartTime(System.currentTimeMillis())
        }

        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildNotification())

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            stopSelf()
            return
        }

        val interval = settingsRepository.getGnssInterval().ms
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, interval)
            .setMinUpdateIntervalMillis(interval)
            .build()

        fusedLocationClient.removeLocationUpdates(locationCallback)
        fusedLocationClient.requestLocationUpdates(
            request,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    private fun stopTracking() {
        settingsRepository.setTrackingState(false)
        settingsRepository.setTrackingStartTime(0L)
        fusedLocationClient.removeLocationUpdates(locationCallback)
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun buildNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Tracking active")
            .setContentText("Recording route...")
            .setSmallIcon(R.drawable.ic_location_button)
            .setOngoing(true)
            .build()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "GNSS Tracking",
            NotificationManager.IMPORTANCE_LOW
        )
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }

    override fun onDestroy() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
        scope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val ACTION_START = "ACTION_START_TRACKING"
        const val ACTION_STOP = "ACTION_STOP_TRACKING"
        const val ACTION_LOCATION_UPDATE = "org.pakicek.monoforecast.LOCATION_UPDATE"
        const val EXTRA_LAT = "lat"
        const val EXTRA_LON = "lon"
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "location_tracking_channel"
    }
}