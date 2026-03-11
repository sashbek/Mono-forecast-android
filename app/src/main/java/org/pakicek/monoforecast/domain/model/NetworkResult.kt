package org.pakicek.monoforecast.domain.model

sealed class NetworkResult<out T> {
    data class Success<T>(val data: T) : NetworkResult<T>()
    data class Error(val code: Int, val message: String?, val exception: Throwable? = null) : NetworkResult<Nothing>()
}