package org.pakicek.monoforecast.presentation.main

import android.app.Application
import org.pakicek.monoforecast.data.api.network.RetrofitClients

class MonoForecastApp : Application() {
    override fun onCreate() {
        super.onCreate()
        RetrofitClients.init(this)
    }
}