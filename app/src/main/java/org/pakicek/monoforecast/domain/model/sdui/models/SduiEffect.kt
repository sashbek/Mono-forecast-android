package org.pakicek.monoforecast.domain.model.sdui.models

sealed class SduiEffect {
    data class Navigate(val path: String) : SduiEffect()
    data object CloseDialog : SduiEffect()
    data class Message(val text: String) : SduiEffect()
}