package org.pakicek.monoforecast.data.repository

import android.net.Uri
import com.google.gson.Gson
import com.google.gson.internal.LinkedTreeMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.pakicek.monoforecast.data.remote.api.EchoApiInterface
import org.pakicek.monoforecast.domain.model.bdui.BduiPage
import org.pakicek.monoforecast.domain.repository.BduiRepository

class BduiRepositoryImpl(
    private val api: EchoApiInterface,
    private val gson: Gson,
    private val prefix: String = "m3300-01-monoforecast"
) : BduiRepository {

    override suspend fun getPage(path: String): Result<BduiPage> = withContext(Dispatchers.IO) {
        runCatching {
            val encoded = Uri.encode(buildEchoKey(path))
            val resp = api.getByPath(encoded)
            if (!resp.isSuccessful) throw IllegalStateException("GET failed: HTTP ${resp.code()}")

            val body = resp.body() ?: throw IllegalStateException("Page is null (maybe deleted): $path")

            val map = body as? LinkedTreeMap<*, *>
                ?: throw IllegalStateException("Unexpected JSON type (expected object)")

            gson.fromJson(gson.toJson(map), BduiPage::class.java)
        }
    }

    override suspend fun putPage(path: String, page: BduiPage): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val encoded = Uri.encode(buildEchoKey(path))
            val resp = api.putByPath(encoded, page)
            if (!resp.isSuccessful) throw IllegalStateException("PUT failed: HTTP ${resp.code()}")
        }
    }

    override suspend fun deletePage(path: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val encoded = Uri.encode(buildEchoKey(path))
            val resp = api.putByPath(encoded, null)
            if (!resp.isSuccessful) throw IllegalStateException("DELETE(PUT null) failed: HTTP ${resp.code()}")
        }
    }

    private fun buildEchoKey(path: String): String {
        val cleaned = path.trim().removePrefix("/")
        return "$prefix/$cleaned"
    }
}