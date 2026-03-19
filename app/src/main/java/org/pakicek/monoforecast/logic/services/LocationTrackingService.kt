package org.pakicek.monoforecast.logic.services

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
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.pakicek.monoforecast.R
import org.pakicek.monoforecast.domain.repositories.LogsRepository
import org.pakicek.monoforecast.domain.repositories.SettingsRepository

class LocationTrackingService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + Job())
    private val repository by lazy { LogsRepository(applicationContext) }

    private val settingsRepository by lazy { SettingsRepository(applicationContext) }
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    companion object {
        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP = "ACTION_STOP"
        const val ACTION_LOCATION_UPDATE = "org.pakicek.monoforecast.LOCATION_UPDATE"
        private const val NOTIFICATION_ID = 123
        private const val CHANNEL_ID = "gnss_tracking_channel"
        private const val TAG = "GNSS_Service"
    }

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                for (location in locationResult.locations) {
                    Log.d(TAG, "Location: ${location.latitude}, ${location.longitude}")

                    serviceScope.launch {
                        repository.saveLocationLog(location.latitude, location.longitude)
                    }

                    val intent = Intent(ACTION_LOCATION_UPDATE).apply {
                        setPackage(packageName)
                        putExtra("lat", location.latitude)
                        putExtra("lon", location.longitude)
                    }
                    sendBroadcast(intent)
                }
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

        val interval = settingsRepository.getGnssInterval().ms
        Log.d(TAG, "Requested Interval: $interval ms")

        createNotificationChannel()
        val notification = buildNotification()
        startForeground(NOTIFICATION_ID, notification)

        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, interval)
            .setMinUpdateIntervalMillis(interval)
            .setMaxUpdateDelayMillis(interval)
            .build()

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            stopSelf()
            return
        }

        fusedLocationClient.removeLocationUpdates(locationCallback)

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
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

    private fun buildNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Tracking Active")
            .setContentText("Recording route...")
            .setSmallIcon(R.drawable.ic_location_button)
            .setOngoing(true)
            .build()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }
}