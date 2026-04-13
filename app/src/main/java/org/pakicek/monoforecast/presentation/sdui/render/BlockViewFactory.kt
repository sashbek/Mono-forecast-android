package org.pakicek.monoforecast.presentation.sdui.render

import android.widget.LinearLayout
import org.pakicek.monoforecast.domain.model.sdui.models.SduiBlock

class BlockViewFactory(
    mappers: List<BlockViewMapper>
) {
    private val registry = mappers.associateBy { it.type }

    fun render(container: LinearLayout, block: SduiBlock, ctx: BlockRenderContext) {
        registry[block.type]?.render(container, block, ctx)
    }
}