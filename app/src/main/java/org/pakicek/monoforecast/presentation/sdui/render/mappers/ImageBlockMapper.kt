package org.pakicek.monoforecast.presentation.sdui.render.mappers

import android.content.Context
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout
import org.pakicek.monoforecast.R
import org.pakicek.monoforecast.domain.model.sdui.str
import org.pakicek.monoforecast.domain.model.sdui.models.SduiBlock
import org.pakicek.monoforecast.presentation.sdui.render.BlockRenderContext
import org.pakicek.monoforecast.presentation.sdui.render.BlockViewMapper

class ImageBlockMapper(
    private val context: Context,
    private val inflater: LayoutInflater
) : BlockViewMapper {

    override val type: String = "image"

    override fun render(container: LinearLayout, block: SduiBlock, ctx: BlockRenderContext) {
        val key = block.data?.str("imageKey").orEmpty()
        val resId = context.resources.getIdentifier(key, "drawable", context.packageName)
        if (resId == 0) return

        val iv = inflater.inflate(R.layout.sdui_item_image, container, false) as ImageView
        iv.setImageResource(resId)
        container.addView(iv)
    }
}