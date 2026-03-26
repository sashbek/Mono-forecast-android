package org.pakicek.monoforecast.presentation.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.pakicek.monoforecast.domain.model.BleState
import org.pakicek.monoforecast.domain.model.GnssState

class NotificationActionReceiver : BroadcastReceiver() {

    private val scope = CoroutineScope(Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        val service = MainService.instance ?: return

        when (intent.action) {
            AppNotificationManager.ACTION_TOGGLE_GNSS -> {
                scope.launch {
                    if (service.gnssFeature.state.value == GnssState.STOPPED) {
                        service.gnssFeature.start()
                    } else {
                        service.gnssFeature.stop()
                    }
                }
            }
            AppNotificationManager.ACTION_TOGGLE_BLE -> {
                scope.launch {
                    if (service.bleFeature.state.value == BleState.DISCONNECTED) {
                        service.bleFeature.start()
                    } else {
                        service.bleFeature.stop()
                    }
                }
            }
            AppNotificationManager.ACTION_REFRESH_WEATHER -> {
                scope.launch {
                    service.weatherFeature.update()
                }
            }
        }
    }
}