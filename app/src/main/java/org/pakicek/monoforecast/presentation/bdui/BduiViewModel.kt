package org.pakicek.monoforecast.presentation.bdui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.pakicek.monoforecast.domain.model.bdui.BduiBlock
import org.pakicek.monoforecast.domain.model.bdui.BduiPage
import org.pakicek.monoforecast.domain.repository.BduiRepository

class BduiViewModel(
    private val repo: BduiRepository
) : ViewModel() {

    private val _state = MutableStateFlow<BduiUiState>(BduiUiState.Loading)
    val state: StateFlow<BduiUiState> = _state.asStateFlow()

    fun load(path: String) {
        _state.value = BduiUiState.Loading
        viewModelScope.launch {
            repo.getPage(path)
                .onSuccess { page -> _state.value = BduiUiState.Content(path, page) }
                .onFailure { e -> _state.value = BduiUiState.Error(path, e.message ?: "Unknown error") }
        }
    }

    fun addPost(title: String, body: String, previewImageKey: String) {
        viewModelScope.launch {
            val postId = "post_${System.currentTimeMillis()}"
            val postPath = "/posts/$postId"

            val postPage = BduiPage(
                title = title,
                blocks = listOf(
                    BduiBlock(type = "image", imageKey = previewImageKey),
                    BduiBlock(type = "text", text = body)
                )
            )

            val putPost = repo.putPage(postPath, postPage)
            if (putPost.isFailure) {
                _state.value = BduiUiState.Error(postPath, putPost.exceptionOrNull()?.message ?: "Failed to save post")
                return@launch
            }

            val mainResult = repo.getPage("/main")
            if (mainResult.isFailure) {
                _state.value = BduiUiState.Error("/main", mainResult.exceptionOrNull()?.message ?: "Failed to load main")
                return@launch
            }

            val main = mainResult.getOrThrow()
            val updatedMain = main.copy(
                blocks = main.blocks + BduiBlock(
                    type = "post_preview",
                    id = postId,
                    title = title,
                    imageKey = previewImageKey,
                    buttonText = "More info",
                    path = postPath
                )
            )

            val putMain = repo.putPage("/main", updatedMain)
            if (putMain.isFailure) {
                _state.value = BduiUiState.Error("/main", putMain.exceptionOrNull()?.message ?: "Failed to update main")
                return@launch
            }

            load("/main")
        }
    }

    fun deletePostFromMain(postId: String?, postPath: String) {
        viewModelScope.launch {
            _state.value = BduiUiState.Loading

            val mainRes = repo.getPage("/main")
            if (mainRes.isFailure) {
                _state.value = BduiUiState.Error("/main", mainRes.exceptionOrNull()?.message ?: "Failed to load main")
                return@launch
            }

            val main = mainRes.getOrThrow()

            val updatedBlocks = main.blocks.filterNot { block ->
                block.type == "post_preview" && (
                        (!postId.isNullOrBlank() && block.id == postId) ||
                                (block.path != null && block.path == postPath)
                        )
            }

            val putMain = repo.putPage("/main", main.copy(blocks = updatedBlocks))
            if (putMain.isFailure) {
                _state.value = BduiUiState.Error("/main", putMain.exceptionOrNull()?.message ?: "Failed to update main")
                return@launch
            }

            repo.deletePage(postPath)

            load("/main")
        }
    }
}