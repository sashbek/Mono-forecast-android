package org.pakicek.monoforecast.di

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import org.pakicek.monoforecast.data.local.db.LogsDb
import org.pakicek.monoforecast.data.remote.api.RetrofitClientFactory
import org.pakicek.monoforecast.data.remote.provider.LocationProvider
import org.pakicek.monoforecast.data.remote.api.EchoApiInterface
import org.pakicek.monoforecast.data.repository.ForecastRepositoryImpl
import org.pakicek.monoforecast.data.repository.LogsRepositoryImpl
import org.pakicek.monoforecast.data.repository.SettingsRepositoryImpl
import org.pakicek.monoforecast.data.repository.BduiRepositoryImpl
import org.pakicek.monoforecast.domain.repository.ForecastRepository
import org.pakicek.monoforecast.domain.repository.LogsRepository
import org.pakicek.monoforecast.domain.repository.SettingsRepository
import org.pakicek.monoforecast.domain.repository.BduiRepository

class AppContainer (private val context: Context) {

    private val gson: Gson = GsonBuilder().create()

    private val database: LogsDb by lazy {
        LogsDb.getInstance(context)
    }

    private val echoOkHttp: OkHttpClient by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()
    }

    private val echoRetrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://alfaitmo.ru/")
            .client(echoOkHttp)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    private val echoApi: EchoApiInterface by lazy {
        echoRetrofit.create(EchoApiInterface::class.java)
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

    val bduiRepository: BduiRepository by lazy {
        BduiRepositoryImpl(
            api = echoApi,
            gson = gson,
            prefix = "m3300-01-monoforecast"
        )
    }
}