package org.pakicek.monoforecast.presentation.bdui

import org.pakicek.monoforecast.domain.model.bdui.BduiPage

sealed class BduiUiState {
    data object Loading : BduiUiState()
    data class Content(val path: String, val page: BduiPage) : BduiUiState()
    data class Error(val path: String, val message: String) : BduiUiState()
}