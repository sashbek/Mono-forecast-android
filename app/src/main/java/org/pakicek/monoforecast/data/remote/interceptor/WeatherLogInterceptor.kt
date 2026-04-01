package org.pakicek.monoforecast.data.remote.interceptor

import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.pakicek.monoforecast.data.remote.dto.MainDto
import org.pakicek.monoforecast.data.remote.dto.WeatherResponseDto
import org.pakicek.monoforecast.domain.repository.LogsRepository
import org.pakicek.monoforecast.data.remote.dto.NinjaWeatherDto
import org.pakicek.monoforecast.data.remote.dto.OpenMeteoDto
import org.pakicek.monoforecast.data.remote.dto.WindDto

class WeatherLogInterceptor(
    private val logsRepository: LogsRepository
) : Interceptor {

    private val scope = CoroutineScope(Dispatchers.IO)
    private val gson = Gson()

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = try {
            chain.proceed(request)
        } catch (e: Exception) {
            return chain.proceed(request)
        }

        if (response.isSuccessful) {
            val url = request.url.toString()
            if (isWeatherApi(url)) {
                val responseBody = response.body
                val content = responseBody.string()

                scope.launch {
                    processAndSaveLog(url, content)
                }

                return response.newBuilder()
                    .body(content.toResponseBody(responseBody.contentType()))
                    .build()
            }
        }
        return response
    }

    private fun isWeatherApi(url: String): Boolean {
        return url.contains("api.api-ninjas.com") || url.contains("api.open-meteo.com")
    }

    private suspend fun processAndSaveLog(url: String, json: String) {
        try {
            val dto = parseResponse(url, json)
            if (dto != null) {
                logsRepository.saveWeatherLog(dto)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun parseResponse(url: String, json: String): WeatherResponseDto? {
        return try {
            if (url.contains("api-ninjas")) {
                val ninjaData = if (json.trim().startsWith("[")) {
                    gson.fromJson(json, Array<NinjaWeatherDto>::class.java).firstOrNull()
                } else {
                    gson.fromJson(json, NinjaWeatherDto::class.java)
                }

                ninjaData?.let {
                    WeatherResponseDto(
                        main = MainDto(it.temp, it.humidity),
                        wind = WindDto(it.windSpeed, it.windDegrees),
                        cloudPct = it.cloudPct,
                        timestamp = System.currentTimeMillis()
                    )
                }
            } else if (url.contains("open-meteo")) {
                val omData = gson.fromJson(json, OpenMeteoDto::class.java)
                val curr = omData.current
                WeatherResponseDto(
                    main = MainDto(curr.temperature, curr.humidity),
                    wind = WindDto(curr.windSpeed, curr.windDirection),
                    cloudPct = curr.cloudCover,
                    timestamp = System.currentTimeMillis()
                )
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}