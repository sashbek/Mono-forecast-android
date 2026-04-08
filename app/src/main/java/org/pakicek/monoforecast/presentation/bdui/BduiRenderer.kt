package org.pakicek.monoforecast.presentation.bdui

import android.content.Context
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.isVisible
import org.pakicek.monoforecast.R
import org.pakicek.monoforecast.domain.model.bdui.BduiBlock

class BduiRenderer(
    private val context: Context,
    private val inflater: LayoutInflater
) {
    fun render(
        container: LinearLayout,
        blocks: List<BduiBlock>,
        isMainPage: Boolean,
        onNavigate: (String) -> Unit,
        onAction: (String) -> Unit,
        onDeletePost: (postId: String?, postPath: String) -> Unit
    ) {
        container.removeAllViews()

        blocks.forEach { block ->
            when (block.type) {
                "post_preview" -> {
                    val v = inflater.inflate(R.layout.item_bdui_post_preview, container, false)

                    val title = v.findViewById<TextView>(R.id.tvTitle)
                    val button = v.findViewById<TextView>(R.id.btnMore)
                    val image = v.findViewById<android.widget.ImageView>(R.id.ivIcon)
                    val deleteBtn = v.findViewById<android.widget.ImageButton>(R.id.btnDelete)

                    title.text = block.title ?: "Untitled"
                    button.text = block.buttonText ?: context.getString(R.string.bdui_more)

                    val resId = drawableKeyToResId(block.imageKey)
                    image.isVisible = resId != 0
                    if (resId != 0) image.setImageResource(resId)

                    val path = block.path
                    v.setOnClickListener { if (path != null) onNavigate(path) }
                    button.setOnClickListener { if (path != null) onNavigate(path) }

                    deleteBtn.isVisible = isMainPage
                    deleteBtn.setOnClickListener {
                        if (!isMainPage) return@setOnClickListener
                        val p = path ?: return@setOnClickListener
                        onDeletePost(block.id, p)
                    }

                    container.addView(v)
                }

                "text" -> {
                    val tv = inflater.inflate(R.layout.item_bdui_text, container, false) as TextView
                    tv.text = block.text.orEmpty()
                    container.addView(tv)
                }

                "image" -> {
                    val iv = inflater.inflate(R.layout.item_bdui_image, container, false) as android.widget.ImageView
                    val resId = drawableKeyToResId(block.imageKey)
                    if (resId != 0) {
                        iv.setImageResource(resId)
                        container.addView(iv)
                    }
                }

                "action_button" -> {
                    val btn = inflater.inflate(R.layout.item_bdui_action_button, container, false)
                            as com.google.android.material.button.MaterialButton
                    btn.text = block.title ?: context.getString(R.string.bdui_action)
                    val action = block.action
                    btn.setOnClickListener { if (action != null) onAction(action) }
                    container.addView(btn)
                }
            }
        }
    }

    private fun drawableKeyToResId(key: String?): Int {
        if (key.isNullOrBlank()) return 0
        return context.resources.getIdentifier(key, "drawable", context.packageName)
    }
}