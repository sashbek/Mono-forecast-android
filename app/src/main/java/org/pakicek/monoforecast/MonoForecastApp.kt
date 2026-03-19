package org.pakicek.monoforecast

import android.app.Application
import org.pakicek.monoforecast.domain.api.network.RetrofitClients

class MonoForecastApp : Application() {
    override fun onCreate() {
        super.onCreate()
        RetrofitClients.init(this)
    }
}