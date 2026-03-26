package org.pakicek.monoforecast

import android.app.Application
import com.yandex.mapkit.MapKitFactory
import org.pakicek.monoforecast.di.AppContainer
import org.pakicek.monoforecast.di.AppContainerImpl

class MonoForecastApp : Application() {

    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppContainerImpl(this)
        MapKitFactory.setApiKey(BuildConfig.MAPKIT_API_KEY)
    }
}