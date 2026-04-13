package org.pakicek.monoforecast.domain.model.sdui.models

import com.google.gson.JsonObject

sealed class SduiEffect {
    data class Navigate(val path: String) : SduiEffect()
    data object CloseDialog : SduiEffect()
    data class Message(val text: String) : SduiEffect()
    data class OpenDialog(val dialog: JsonObject, val baseVars: TemplateVars) : SduiEffect()
}