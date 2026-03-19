package org.pakicek.monoforecast

import android.app.Application
import com.yandex.mapkit.MapKitFactory
import org.pakicek.monoforecast.domain.api.network.RetrofitClients

class MonoForecastApp : Application() {
    override fun onCreate() {
        super.onCreate()
        RetrofitClients.init(this)

        MapKitFactory.setApiKey(BuildConfig.MAPKIT_API_KEY)
        MapKitFactory.initialize(this)
    }
}