package org.pakicek.monoforecast.presentation.sdui.render.mappers

import android.content.Context
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.google.android.material.button.MaterialButton
import org.pakicek.monoforecast.R
import org.pakicek.monoforecast.domain.model.sdui.SduiParser
import org.pakicek.monoforecast.domain.model.sdui.obj
import org.pakicek.monoforecast.domain.model.sdui.str
import org.pakicek.monoforecast.domain.model.sdui.models.SduiBlock
import org.pakicek.monoforecast.presentation.sdui.render.BlockRenderContext
import org.pakicek.monoforecast.presentation.sdui.render.BlockViewMapper

class ButtonBlockMapper(
    private val context: Context,
    private val inflater: LayoutInflater
) : BlockViewMapper {

    override val type: String = "button"

    override fun render(container: LinearLayout, block: SduiBlock, ctx: BlockRenderContext) {
        val actionObj = block.data?.obj("action") ?: return
        val action = SduiParser.parseAction(actionObj)

        val btn = inflater.inflate(R.layout.sdui_item_action_button, container, false) as MaterialButton
        btn.text = block.data.str("text") ?: context.getString(R.string.bdui_action)
        btn.setOnClickListener { ctx.onAction(action) }

        container.addView(btn)
    }
}