package org.pakicek.monoforecast.domain.model.sdui.models

import com.google.gson.JsonObject

data class SduiAction(
    val type: String,
    val data: JsonObject?
)