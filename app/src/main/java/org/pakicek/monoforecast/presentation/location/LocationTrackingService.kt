package org.pakicek.monoforecast.presentation.location

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import android.os.Looper
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
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.pakicek.monoforecast.MonoForecastApp
import org.pakicek.monoforecast.R

class LocationTrackingService : Service() {
    private val scope = CoroutineScope(Dispatchers.IO)
    private lateinit var client: FusedLocationProviderClient
    private lateinit var callback: LocationCallback

    companion object {
        const val ACTION_START = "START"
        const val ACTION_STOP = "STOP"
        const val ACTION_UPDATE = "UPDATE"
        const val ACTION_STOPPED = "STOPPED"
        private const val NOTIFICATION_ID = 123
        private const val CHANNEL_ID = "GPS_TRACKING"
    }

    override fun onCreate() {
        super.onCreate()
        client = LocationServices.getFusedLocationProviderClient(this)
        callback = object : LocationCallback() {
            override fun onLocationResult(res: LocationResult) {
                res.lastLocation?.let {
                    val repo = (application as MonoForecastApp).container.logsRepository
                    scope.launch { repo.saveLocationLog(it.latitude, it.longitude) }

                    sendBroadcast(Intent(ACTION_UPDATE).apply {
                        setPackage(packageName)
                        putExtra("lat", it.latitude)
                        putExtra("lon", it.longitude)
                    })
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopTracking()
            return START_NOT_STICKY
        }

        startForeground(NOTIFICATION_ID, buildNotification())

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            val settings = (application as MonoForecastApp).container.settingsRepository
            val interval = settings.getGnssInterval().ms

            val req = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, interval)
                .setMinUpdateIntervalMillis(interval)
                .build()

            client.requestLocationUpdates(req, callback, Looper.getMainLooper())
        } else {
            stopSelf()
        }
        return START_STICKY
    }

    private fun stopTracking() {
        client.removeLocationUpdates(callback)

        val settings = (application as MonoForecastApp).container.settingsRepository
        settings.setTrackingState(false)

        sendBroadcast(Intent(ACTION_STOPPED).apply {
            setPackage(packageName)
        })

        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun buildNotification(): android.app.Notification {
        val channel = NotificationChannel(CHANNEL_ID, "Tracking", NotificationManager.IMPORTANCE_LOW)
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)

        val contentIntent = Intent(this, LocationActivity::class.java)
        val contentPendingIntent = PendingIntent.getActivity(
            this, 0, contentIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val stopIntent = Intent(this, LocationTrackingService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this, 1, stopIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_CANCEL_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_location_button)
            .setContentTitle(getString(R.string.notification_tracking_title))
            .setContentText(getString(R.string.notification_tracking_desc))
            .setContentIntent(contentPendingIntent)
            .addAction(R.drawable.ic_close_button, getString(R.string.stop_tracking_button_text), stopPendingIntent)
            .setOngoing(true)
            .build()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }
}