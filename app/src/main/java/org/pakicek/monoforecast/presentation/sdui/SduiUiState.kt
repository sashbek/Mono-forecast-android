package org.pakicek.monoforecast.presentation.sdui

import org.pakicek.monoforecast.domain.model.sdui.models.SduiPage

sealed class SduiUiState {
    data object Loading : SduiUiState()
    data class Content(val path: String, val page: SduiPage, val rawJson: String) : SduiUiState()
    data class Error(val path: String, val message: String) : SduiUiState()
}