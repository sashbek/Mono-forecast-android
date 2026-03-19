package org.pakicek.monoforecast.domain.api.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.pakicek.monoforecast.domain.model.NetworkResult
import retrofit2.Response

suspend fun <T> safeApiCall(apiCall: suspend () -> Response<T>): NetworkResult<T> {
    return withContext(Dispatchers.IO) {
        try {
            val response = apiCall()
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    NetworkResult.Success(body)
                } else {
                    NetworkResult.Error(response.code(), "Response body is null")
                }
            } else {
                NetworkResult.Error(response.code(), response.message())
            }
        } catch (e: Exception) {
            NetworkResult.Error(-1, e.message, e)
        }
    }
}