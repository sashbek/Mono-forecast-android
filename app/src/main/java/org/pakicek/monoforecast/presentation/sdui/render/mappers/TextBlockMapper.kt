package org.pakicek.monoforecast.presentation.sdui.render.mappers

import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import org.pakicek.monoforecast.R
import org.pakicek.monoforecast.domain.model.sdui.str
import org.pakicek.monoforecast.domain.model.sdui.models.SduiBlock
import org.pakicek.monoforecast.presentation.sdui.render.BlockRenderContext
import org.pakicek.monoforecast.presentation.sdui.render.BlockViewMapper

class TextBlockMapper(
    private val inflater: LayoutInflater
) : BlockViewMapper {

    override val type: String = "text"

    override fun render(container: LinearLayout, block: SduiBlock, ctx: BlockRenderContext) {
        val tv = inflater.inflate(R.layout.sdui_item_text, container, false) as TextView
        tv.text = block.data?.str("text").orEmpty()
        container.addView(tv)
    }
}