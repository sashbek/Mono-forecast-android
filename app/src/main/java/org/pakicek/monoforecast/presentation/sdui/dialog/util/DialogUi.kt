package org.pakicek.monoforecast.presentation.sdui.dialog.util

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.google.android.material.textfield.TextInputLayout

object DialogUi {

    fun lpMatchWrap(): ViewGroup.LayoutParams =
        LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

    fun dp(ctx: Context, v: Int): Int =
        (v * ctx.resources.displayMetrics.density).toInt()

    fun space(ctx: Context, dp: Int): View =
        View(ctx).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(ctx, dp)
            )
        }

    fun outlinedTil(ctx: Context): TextInputLayout =
        TextInputLayout(ctx).apply {
            layoutParams = lpMatchWrap()
            boxBackgroundMode = TextInputLayout.BOX_BACKGROUND_OUTLINE
        }
}