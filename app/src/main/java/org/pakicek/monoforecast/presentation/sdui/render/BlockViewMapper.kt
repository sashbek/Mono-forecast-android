package org.pakicek.monoforecast.presentation.sdui.render

import android.widget.LinearLayout
import org.pakicek.monoforecast.domain.model.sdui.models.SduiBlock

interface BlockViewMapper {
    val type: String
    fun render(container: LinearLayout, block: SduiBlock, ctx: BlockRenderContext)
}