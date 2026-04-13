package org.pakicek.monoforecast.domain.model.sdui.models

import com.google.gson.JsonObject

data class SduiBlock(
    val type: String,
    val id: String?,
    val layout: SduiLayout?,
    val data: JsonObject?
)