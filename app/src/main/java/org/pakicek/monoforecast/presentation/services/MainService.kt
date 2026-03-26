package org.pakicek.monoforecast.presentation.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.combine
import org.pakicek.monoforecast.MonoForecastApp
import org.pakicek.monoforecast.data.features.BleFeature
import org.pakicek.monoforecast.data.features.GnssFeature
import org.pakicek.monoforecast.data.features.WeatherFeature
import org.pakicek.monoforecast.domain.model.BleState
import org.pakicek.monoforecast.domain.model.GnssState

class MainService : Service() {

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private lateinit var notifManager: AppNotificationManager

    lateinit var gnssFeature: GnssFeature
    lateinit var weatherFeature: WeatherFeature
    lateinit var bleFeature: BleFeature

    companion object {
        var isRunning = false
        var instance: MainService? = null

        val deviceFound get() = instance?.bleFeature?.deviceFound
        val metricsUpdate get() = instance?.bleFeature?.metricsUpdate
        val error get() = instance?.bleFeature?.error
        val connectionState get() = instance?.bleFeature?.connectionState
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        isRunning = true

        val container = (application as MonoForecastApp).container
        notifManager = AppNotificationManager(this)

        gnssFeature = GnssFeature(this, container.settingsRepository, container.logsRepository)
        weatherFeature = WeatherFeature(this, container.forecastRepository)
        bleFeature = BleFeature(this)

        observeStates()

        scope.launch { weatherFeature.loadCache() }
    }

    private fun observeStates() {
        scope.launch {
            combine(
                gnssFeature.state,
                bleFeature.state,
                weatherFeature.state
            ) { gnss, ble, weather ->
                Triple(gnss, ble, weather)
            }.collect { (gnss, ble, weather) ->
                val notif = notifManager.buildNotification(gnss, ble, weather)
                notifManager.updateNotification(notif)
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val initialNotif = notifManager.buildNotification(GnssState.STOPPED, BleState.DISCONNECTED, null)
        startForeground(AppNotificationManager.NOTIFICATION_ID, initialNotif)

        return START_STICKY
    }

    override fun onDestroy() {
        isRunning = false
        instance = null
        scope.cancel()
        runBlocking {
            gnssFeature.stop()
            bleFeature.stop()
        }
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}