package org.pakicek.monoforecast

import android.app.Application
import com.yandex.mapkit.MapKitFactory
import org.pakicek.monoforecast.di.AppContainer

class MonoForecastApp : Application() {

    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        MapKitFactory.setApiKey(BuildConfig.MAPKIT_API_KEY)
    }
}