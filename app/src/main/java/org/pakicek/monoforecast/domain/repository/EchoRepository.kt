package org.pakicek.monoforecast.domain.repository

import com.google.gson.JsonElement

interface EchoRepository {
    suspend fun get(path: String): Result<JsonElement>
    suspend fun put(path: String, body: JsonElement): Result<Unit>
}