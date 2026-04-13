package org.pakicek.monoforecast.data.repository

import android.net.Uri
import com.google.gson.JsonElement
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.pakicek.monoforecast.data.remote.api.EchoApiInterface
import org.pakicek.monoforecast.domain.repository.EchoRepository

class EchoRepositoryImpl(
    private val api: EchoApiInterface,
    private val prefix: String = "m3300-01-monoforecast"
) : EchoRepository {

    override suspend fun get(path: String): Result<JsonElement> = withContext(Dispatchers.IO) {
        runCatching {
            val encodedKey = Uri.encode(buildEchoKey(path))
            val resp = api.getByPath(encodedKey)
            if (!resp.isSuccessful) error("GET failed: HTTP ${resp.code()}")
            resp.body() ?: error("Echo record is null: $path")
        }
    }

    override suspend fun put(path: String, body: JsonElement): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val encodedKey = Uri.encode(buildEchoKey(path))
            val resp = api.putByPath(encodedKey, body)
            if (!resp.isSuccessful) error("PUT failed: HTTP ${resp.code()}")
        }
    }

    private fun buildEchoKey(path: String): String {
        val cleaned = path.trim().removePrefix("/")
        return "$prefix/$cleaned"
    }
}