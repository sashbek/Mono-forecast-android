package org.pakicek.monoforecast.domain.api.network

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.pakicek.monoforecast.domain.model.dto.NinjaWeatherDto
import org.pakicek.monoforecast.domain.model.dto.OpenMeteoDto
import org.pakicek.monoforecast.domain.model.dto.WeatherResponseDto
import org.pakicek.monoforecast.domain.model.dto.MainDto
import org.pakicek.monoforecast.domain.model.dto.WindDto
import org.pakicek.monoforecast.domain.repositories.LogsRepository

class WeatherLogInterceptor(context: Context) : Interceptor {

    private val logsRepository by lazy { LogsRepository(context) }
    private val scope = CoroutineScope(Dispatchers.IO)
    private val gson = Gson()

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
                        val dto = parseResponse(url, content)
                        if (dto != null) {
                            logsRepository.saveWeatherLog(dto)
                            Log.d("WeatherLogInterceptor", "Forecast saved to logs.")
                        }
                    } catch (e: Exception) {
                        Log.e("WeatherLogInterceptor", "Failed to log weather", e)
                    }
                }

                return response.newBuilder()
                    .body(content.toResponseBody(responseBody.contentType()))
                    .build()
            }
        }
        return response
    }

    private fun parseResponse(url: String, json: String): WeatherResponseDto? {
        return try {
            if (url.contains("api-ninjas")) {
                val ninjaData = if (json.trim().startsWith("[")) {
                    val list = gson.fromJson(json, Array<NinjaWeatherDto>::class.java)
                    list.firstOrNull()
                } else {
                    gson.fromJson(json, NinjaWeatherDto::class.java)
                }

                if (ninjaData != null) {
                    WeatherResponseDto(
                        main = MainDto(
                            temp = ninjaData.temp,
                            humidity = ninjaData.humidity
                        ),
                        wind = WindDto(
                            speed = ninjaData.windSpeed,
                            direction = ninjaData.windDegrees
                        ),
                        cloudPct = ninjaData.cloudPct,
                        timestamp = System.currentTimeMillis()
                    )
                } else null

            } else if (url.contains("open-meteo")) {
                val omData = gson.fromJson(json, OpenMeteoDto::class.java)
                val curr = omData.current

                WeatherResponseDto(
                    main = MainDto(
                        temp = curr.temperature,
                        humidity = curr.humidity
                    ),
                    wind = WindDto(
                        speed = curr.windSpeed,
                        direction = curr.windDirection
                    ),
                    cloudPct = curr.cloudCover,
                    timestamp = System.currentTimeMillis()
                )
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("WeatherLogInterceptor", "JSON Parse Error", e)
            null
        }
    }
}