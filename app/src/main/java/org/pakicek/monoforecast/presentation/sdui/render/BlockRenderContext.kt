package org.pakicek.monoforecast.presentation.sdui.render

import org.pakicek.monoforecast.domain.model.sdui.models.SduiAction
import org.pakicek.monoforecast.domain.model.sdui.models.SduiBlock

data class BlockRenderContext(
    val isMainPage: Boolean,
    val onAction: (SduiAction) -> Unit,
    val onDeleteAction: (SduiAction) -> Unit,
    val renderBlocks: (List<SduiBlock>) -> Unit
)