package org.pakicek.monoforecast.presentation.sdui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.GsonBuilder
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.pakicek.monoforecast.domain.model.sdui.SduiParser
import org.pakicek.monoforecast.domain.model.sdui.models.SduiAction
import org.pakicek.monoforecast.domain.model.sdui.models.SduiEffect
import org.pakicek.monoforecast.domain.model.sdui.models.TemplateVars
import org.pakicek.monoforecast.domain.repository.EchoRepository
import org.pakicek.monoforecast.presentation.sdui.engine.SduiActionEngine

class SduiViewModel(
    private val repo: EchoRepository
) : ViewModel() {

    private val engine = SduiActionEngine(repo)
    private val prettyGson = GsonBuilder().setPrettyPrinting().create()

    private val _state = MutableStateFlow<SduiUiState>(SduiUiState.Loading)
    val state = _state.asStateFlow()

    private val _effects = MutableSharedFlow<SduiEffect>(extraBufferCapacity = 32)
    val effects = _effects.asSharedFlow()

    fun load(path: String) {
        _state.value = SduiUiState.Loading
        viewModelScope.launch {
            repo.get(path)
                .onSuccess { json ->
                    val page = SduiParser.parsePage(json)
                    val raw = prettyGson.toJson(json)
                    _state.value = SduiUiState.Content(path, page, raw)
                }
                .onFailure { e ->
                    _state.value = SduiUiState.Error(path, e.message ?: "Unknown error")
                }
        }
    }

    fun runAction(action: SduiAction, vars: TemplateVars, currentPath: String) {
        viewModelScope.launch {
            runCatching { engine.execute(action, vars) }
                .onSuccess { effects ->
                    effects.forEach { _effects.tryEmit(it) }
                    val hasNavigate = effects.any { it is SduiEffect.Navigate }
                    if (!hasNavigate) load(currentPath)
                }
                .onFailure { e ->
                    _effects.tryEmit(SduiEffect.Message(e.message ?: "Action failed"))
                }
        }
    }
}