package org.pakicek.monoforecast.presentation.sdui

import android.content.Context
import android.view.LayoutInflater
import android.widget.LinearLayout
import org.pakicek.monoforecast.domain.model.sdui.models.SduiAction
import org.pakicek.monoforecast.domain.model.sdui.models.SduiBlock
import org.pakicek.monoforecast.presentation.sdui.render.BlockRenderContext
import org.pakicek.monoforecast.presentation.sdui.render.BlockViewFactory
import org.pakicek.monoforecast.presentation.sdui.render.mappers.*

class SduiRenderer(
    context: Context,
    inflater: LayoutInflater
) {
    private val factory = BlockViewFactory(
        listOf(
            ScrollBlockMapper(),
            TextBlockMapper(inflater),
            ImageBlockMapper(context, inflater),
            ButtonBlockMapper(context, inflater),
            PostPreviewBlockMapper(context, inflater)
        )
    )

    fun render(
        container: LinearLayout,
        blocks: List<SduiBlock>,
        isMainPage: Boolean,
        onAction: (SduiAction) -> Unit,
        onDeleteAction: (SduiAction) -> Unit
    ) {
        container.removeAllViews()

        lateinit var ctx: BlockRenderContext

        fun renderList(list: List<SduiBlock>) {
            val normalized = normalizePinnedBottom(list)
            normalized.forEach { factory.render(container, it, ctx) }
        }

        ctx = BlockRenderContext(
            isMainPage = isMainPage,
            onAction = onAction,
            onDeleteAction = onDeleteAction,
            renderBlocks = ::renderList
        )

        renderList(blocks)
    }

    private fun normalizePinnedBottom(blocks: List<SduiBlock>): List<SduiBlock> {
        val (top, bottom) = blocks.partition { it.layout?.pin != "bottom" }
        return top + bottom
    }
}