package org.pakicek.monoforecast.di

import android.content.Context
import org.pakicek.monoforecast.data.local.db.LogsDb
import org.pakicek.monoforecast.data.remote.api.RetrofitClientFactory
import org.pakicek.monoforecast.data.remote.provider.LocationProvider
import org.pakicek.monoforecast.data.repository.ForecastRepositoryImpl
import org.pakicek.monoforecast.data.repository.LogsRepositoryImpl
import org.pakicek.monoforecast.data.repository.SettingsRepositoryImpl
import org.pakicek.monoforecast.domain.repository.ForecastRepository
import org.pakicek.monoforecast.domain.repository.LogsRepository
import org.pakicek.monoforecast.domain.repository.SettingsRepository

class AppContainer (private val context: Context) {

    private val database: LogsDb by lazy {
        LogsDb.getInstance(context)
    }

    val logsRepository: LogsRepository by lazy {
        LogsRepositoryImpl(database.logsDao())
    }

    val settingsRepository: SettingsRepository by lazy {
        SettingsRepositoryImpl(context)
    }

    private val retrofitClientFactory: RetrofitClientFactory by lazy {
        RetrofitClientFactory(logsRepository)
    }

    private val locationProvider: LocationProvider by lazy {
        LocationProvider(context)
    }

    val forecastRepository: ForecastRepository by lazy {
        ForecastRepositoryImpl(
            context,
            settingsRepository,
            locationProvider,
            retrofitClientFactory.ninjaApi,
            retrofitClientFactory.openMeteoApi
        )
    }
}