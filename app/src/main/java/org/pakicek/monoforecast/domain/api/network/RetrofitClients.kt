package org.pakicek.monoforecast.domain.api.network

import android.content.Context
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClients {
    private var appContext: Context? = null

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val httpClient: OkHttpClient by lazy {
        val builder = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)

        appContext?.let {
            builder.addInterceptor(WeatherLogInterceptor(it))
        }

        builder.build()
    }

    val ninjaApi: WeatherApiInterface by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.api-ninjas.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(httpClient)
            .build()
            .create(WeatherApiInterface::class.java)
    }

    val openMeteoApi: OpenMeteoApiInterface by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.open-meteo.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(httpClient)
            .build()
            .create(OpenMeteoApiInterface::class.java)
    }
}