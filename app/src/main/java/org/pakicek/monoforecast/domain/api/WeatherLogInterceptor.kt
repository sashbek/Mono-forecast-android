package org.pakicek.monoforecast.domain.api

import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.pakicek.monoforecast.domain.model.dto.enums.LogType
import org.pakicek.monoforecast.domain.repositories.LogsRepository

class WeatherLogInterceptor(private val context: Context) : Interceptor {

    private val logsRepository by lazy { LogsRepository(context) }
    private val scope = CoroutineScope(Dispatchers.IO)

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)

        if (response.isSuccessful) {
            val url = request.url.toString()

            if (url.contains("api.api-ninjas.com") || url.contains("api.open-meteo.com")) {

                val responseBody = response.body
                val content = responseBody.string()

                scope.launch {
                    try {
                        val shortContent = if (content.length > 500) content.take(500) + "..." else content
                        val message = "URL: $url\nResponse: $shortContent"

                        logsRepository.addLog(LogType.WEATHER, message)
                        Log.d("WeatherLogInterceptor", "Saved to DB: $message")
                    } catch (e: Exception) {
                        Log.e("WeatherLogInterceptor", "Failed to save log", e)
                    }
                }

                return response.newBuilder()
                    .body(content.toResponseBody(responseBody.contentType()))
                    .build()
            }
        }

        return response
    }
}