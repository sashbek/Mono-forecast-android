package org.pakicek.monoforecast.presentation.services

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import org.pakicek.monoforecast.R
import org.pakicek.monoforecast.domain.model.BleState
import org.pakicek.monoforecast.domain.model.GnssState
import org.pakicek.monoforecast.domain.model.dto.WeatherResponseDto
import org.pakicek.monoforecast.presentation.main.MainActivity

class AppNotificationManager(private val context: Context) {
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    companion object {
        const val NOTIFICATION_ID = 999
        const val CHANNEL_ID = "mono_forecast_channel"
        const val ACTION_TOGGLE_GNSS = "org.pakicek.monoforecast.ACTION_TOGGLE_GNSS"
        const val ACTION_REFRESH_WEATHER = "org.pakicek.monoforecast.ACTION_REFRESH_WEATHER"
        const val ACTION_TOGGLE_BLE = "org.pakicek.monoforecast.ACTION_TOGGLE_BLE"
    }

    init {
        createChannel()
    }

    @SuppressLint("RemoteViewLayout")
    fun buildNotification(
        gnssState: GnssState,
        bleState: BleState,
        weather: WeatherResponseDto?
    ): Notification {
        val remoteViews = RemoteViews(context.packageName, R.layout.notification_control_panel)

        val tempText = weather?.main?.temp?.let { "%.1f°C".format(it) } ?: "--"
        remoteViews.setTextViewText(R.id.notif_weather_text, tempText)

        val (gnssIcon, gnssAction) = when (gnssState) {
            GnssState.STOPPED -> R.drawable.ic_play to ACTION_TOGGLE_GNSS
            GnssState.RUNNING -> R.drawable.ic_stop to ACTION_TOGGLE_GNSS
            GnssState.LOST -> R.drawable.ic_warning to ACTION_TOGGLE_GNSS
        }
        remoteViews.setImageViewResource(R.id.btn_gnss, gnssIcon)
        remoteViews.setOnClickPendingIntent(R.id.btn_gnss, createActionIntent(gnssAction))

        // BLE Button
        val (bleIcon, bleAction) = when (bleState) {
            BleState.DISCONNECTED -> R.drawable.ic_bluetooth_disabled to ACTION_TOGGLE_BLE
            BleState.CONNECTED -> R.drawable.ic_bluetooth_connected to ACTION_TOGGLE_BLE
            BleState.CONNECTING -> R.drawable.ic_sync to ACTION_TOGGLE_BLE
            BleState.SCANNING -> R.drawable.ic_bluetooth_disabled to ACTION_TOGGLE_BLE
        }
        remoteViews.setImageViewResource(R.id.btn_ble, bleIcon)
        remoteViews.setOnClickPendingIntent(R.id.btn_ble, createActionIntent(bleAction))

        // Refresh Button
        remoteViews.setOnClickPendingIntent(R.id.btn_refresh, createActionIntent(ACTION_REFRESH_WEATHER))

        val statusMsg = "GNSS: ${gnssState.name} | BLE: ${bleState.name}"
        remoteViews.setTextViewText(R.id.notif_status_text, statusMsg)

        val mainIntent = Intent(context, MainActivity::class.java)
        val mainPendingIntent = PendingIntent.getActivity(context, 0, mainIntent, PendingIntent.FLAG_IMMUTABLE)

        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Иконка в статус баре
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setCustomContentView(remoteViews)
            .setContentIntent(mainPendingIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .build()
    }

    fun updateNotification(notification: Notification) {
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun createActionIntent(action: String): PendingIntent {
        val intent = Intent(context, NotificationActionReceiver::class.java).apply {
            this.action = action
        }
        return PendingIntent.getBroadcast(
            context,
            action.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, "Dashboard", NotificationManager.IMPORTANCE_LOW)
            channel.description = "Controls for Mono Forecast"
            notificationManager.createNotificationChannel(channel)
        }
    }
}