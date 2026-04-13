package org.pakicek.monoforecast.presentation.sdui.render.mappers

import android.content.Context
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.isVisible
import org.pakicek.monoforecast.R
import org.pakicek.monoforecast.domain.model.sdui.SduiParser
import org.pakicek.monoforecast.domain.model.sdui.obj
import org.pakicek.monoforecast.domain.model.sdui.str
import org.pakicek.monoforecast.domain.model.sdui.models.SduiBlock
import org.pakicek.monoforecast.presentation.sdui.render.BlockRenderContext
import org.pakicek.monoforecast.presentation.sdui.render.BlockViewMapper

class PostPreviewBlockMapper(
    private val context: Context,
    private val inflater: LayoutInflater
) : BlockViewMapper {

    override val type: String = "post_preview"

    override fun render(container: LinearLayout, block: SduiBlock, ctx: BlockRenderContext) {
        val v = inflater.inflate(R.layout.sdui_item_post_preview, container, false)

        val title = v.findViewById<TextView>(R.id.tvTitle)
        val more = v.findViewById<TextView>(R.id.btnMore)
        val icon = v.findViewById<android.widget.ImageView>(R.id.ivIcon)
        val deleteBtn = v.findViewById<android.widget.ImageButton>(R.id.btnDelete)

        val data = block.data
        title.text = data?.str("title") ?: "Untitled"
        more.text = data?.str("buttonText") ?: context.getString(R.string.bdui_more)

        val iconKey = data?.str("imageKey")
        val resId = if (iconKey.isNullOrBlank()) 0 else context.resources.getIdentifier(iconKey, "drawable", context.packageName)
        icon.isVisible = resId != 0
        if (resId != 0) icon.setImageResource(resId)

        val primary = data?.obj("primaryAction")?.let { SduiParser.parseAction(it) }
        val secondary = data?.obj("secondaryAction")?.let { SduiParser.parseAction(it) }

        v.setOnClickListener { if (primary != null) ctx.onAction(primary) }
        more.setOnClickListener { if (primary != null) ctx.onAction(primary) }

        deleteBtn.isVisible = ctx.isMainPage && secondary != null
        deleteBtn.setOnClickListener { if (secondary != null) ctx.onDeleteAction(secondary) }

        container.addView(v)
    }
}