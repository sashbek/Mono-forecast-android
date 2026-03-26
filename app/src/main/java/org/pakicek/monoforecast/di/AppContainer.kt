package org.pakicek.monoforecast.di

import android.content.Context
import org.pakicek.monoforecast.data.api.RetrofitClientFactory
import org.pakicek.monoforecast.data.dao.LogsDb
import org.pakicek.monoforecast.data.repositories.ForecastRepository
import org.pakicek.monoforecast.data.repositories.LocationProvider
import org.pakicek.monoforecast.data.repositories.LogsRepository
import org.pakicek.monoforecast.data.repositories.SettingsRepository
import org.pakicek.monoforecast.domain.repository.IForecastRepository
import org.pakicek.monoforecast.domain.repository.ILogsRepository
import org.pakicek.monoforecast.domain.repository.ISettingsRepository

interface AppContainer {
    val logsRepository: ILogsRepository
    val settingsRepository: ISettingsRepository
    val forecastRepository: IForecastRepository
}

class AppContainerImpl(private val context: Context) : AppContainer {

    private val database: LogsDb by lazy {
        LogsDb.getInstance(context)
    }

    override val logsRepository: ILogsRepository by lazy {
        LogsRepository(database.logsDao())
    }

    override val settingsRepository: ISettingsRepository by lazy {
        SettingsRepository(context)
    }

    private val retrofitClientFactory: RetrofitClientFactory by lazy {
        RetrofitClientFactory(logsRepository)
    }

    private val locationProvider: LocationProvider by lazy {
        LocationProvider(context)
    }

    override val forecastRepository: IForecastRepository by lazy {
        ForecastRepository(
            context,
            settingsRepository,
            locationProvider,
            retrofitClientFactory.ninjaApi,
            retrofitClientFactory.openMeteoApi
        )
    }
}