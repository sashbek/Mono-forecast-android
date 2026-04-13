package org.pakicek.monoforecast.presentation.sdui.render.mappers

import android.widget.LinearLayout
import org.pakicek.monoforecast.domain.model.sdui.SduiParser
import org.pakicek.monoforecast.domain.model.sdui.arr
import org.pakicek.monoforecast.domain.model.sdui.asObjOrNull
import org.pakicek.monoforecast.domain.model.sdui.models.SduiBlock
import org.pakicek.monoforecast.presentation.sdui.render.BlockRenderContext
import org.pakicek.monoforecast.presentation.sdui.render.BlockViewMapper

class ScrollBlockMapper : BlockViewMapper {

    override val type: String = "scroll"

    override fun render(container: LinearLayout, block: SduiBlock, ctx: BlockRenderContext) {
        val arr = block.data?.arr("blocks") ?: return
        val nested = arr.mapNotNull { it.asObjOrNull() }.map { SduiParser.parseBlock(it) }
        ctx.renderBlocks(nested)
    }
}