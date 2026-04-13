package org.pakicek.monoforecast.presentation.sdui

import android.content.Context
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.isVisible
import com.google.android.material.button.MaterialButton
import org.pakicek.monoforecast.R
import org.pakicek.monoforecast.domain.model.sdui.*
import org.pakicek.monoforecast.domain.model.sdui.models.SduiAction
import org.pakicek.monoforecast.domain.model.sdui.models.SduiBlock

class SduiRenderer(
    private val context: Context,
    private val inflater: LayoutInflater
) {

    fun render(
        container: LinearLayout,
        blocks: List<SduiBlock>,
        isMainPage: Boolean,
        onAction: (SduiAction) -> Unit,
        onDeleteAction: (SduiAction) -> Unit
    ) {
        container.removeAllViews()

        val flattened = flattenBlocks(blocks)
        val normalized = normalizePinnedBottom(flattened)

        normalized.forEach { block ->
            when (block.type) {
                "text" -> renderText(container, block)
                "image" -> renderImage(container, block)
                "button" -> renderButton(container, block, onAction)
                "post_preview" -> renderPostPreview(container, block, isMainPage, onAction, onDeleteAction)
            }
        }
    }

    private fun flattenBlocks(blocks: List<SduiBlock>): List<SduiBlock> {
        val out = mutableListOf<SduiBlock>()
        for (b in blocks) {
            if (b.type == "scroll") {
                val nestedArr = b.data?.arr("blocks")
                val nested = nestedArr
                    ?.mapNotNull { it.asObjOrNull() }
                    ?.map { SduiParser.parseBlock(it) }
                    .orEmpty()
                out += nested
            } else {
                out += b
            }
        }
        return out
    }

    private fun normalizePinnedBottom(blocks: List<SduiBlock>): List<SduiBlock> {
        val (top, bottom) = blocks.partition { it.layout?.pin != "bottom" }
        return top + bottom
    }

    private fun renderText(container: LinearLayout, block: SduiBlock) {
        val tv = inflater.inflate(R.layout.sdui_item_text, container, false) as TextView
        tv.text = block.data?.str("text").orEmpty()
        container.addView(tv)
    }

    private fun renderImage(container: LinearLayout, block: SduiBlock) {
        val imageKey = block.data?.str("imageKey").orEmpty()
        val resId = drawableKeyToResId(imageKey)
        if (resId == 0) return

        val iv = inflater.inflate(R.layout.sdui_item_image, container, false) as android.widget.ImageView
        iv.setImageResource(resId)
        container.addView(iv)
    }

    private fun renderButton(container: LinearLayout, block: SduiBlock, onAction: (SduiAction) -> Unit) {
        val btn = inflater.inflate(R.layout.sdui_item_action_button, container, false) as MaterialButton
        btn.text = block.data?.str("text") ?: context.getString(R.string.bdui_action)

        val actionObj = block.data?.obj("action") ?: return
        val action = SduiParser.parseAction(actionObj)

        btn.setOnClickListener { onAction(action) }
        container.addView(btn)
    }

    private fun renderPostPreview(
        container: LinearLayout,
        block: SduiBlock,
        isMainPage: Boolean,
        onAction: (SduiAction) -> Unit,
        onDeleteAction: (SduiAction) -> Unit
    ) {
        val v = inflater.inflate(R.layout.sdui_item_post_preview, container, false)

        val title = v.findViewById<TextView>(R.id.tvTitle)
        val more = v.findViewById<TextView>(R.id.btnMore)
        val icon = v.findViewById<android.widget.ImageView>(R.id.ivIcon)
        val deleteBtn = v.findViewById<android.widget.ImageButton>(R.id.btnDelete)

        val data = block.data
        title.text = data?.str("title") ?: "Untitled"
        more.text = data?.str("buttonText") ?: context.getString(R.string.bdui_more)

        val resId = drawableKeyToResId(data?.str("imageKey"))
        icon.isVisible = resId != 0
        if (resId != 0) icon.setImageResource(resId)

        val primary = data?.obj("primaryAction")?.let { SduiParser.parseAction(it) }
        val secondary = data?.obj("secondaryAction")?.let { SduiParser.parseAction(it) }

        v.setOnClickListener { if (primary != null) onAction(primary) }
        more.setOnClickListener { if (primary != null) onAction(primary) }

        deleteBtn.isVisible = isMainPage && secondary != null
        deleteBtn.setOnClickListener { if (secondary != null) onDeleteAction(secondary) }

        container.addView(v)
    }

    private fun drawableKeyToResId(key: String?): Int {
        if (key.isNullOrBlank()) return 0
        return context.resources.getIdentifier(key, "drawable", context.packageName)
    }
}