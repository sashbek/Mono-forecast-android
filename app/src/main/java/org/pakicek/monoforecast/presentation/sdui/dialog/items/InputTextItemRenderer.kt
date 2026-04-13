package org.pakicek.monoforecast.presentation.sdui.dialog.items

import android.text.InputType
import android.util.TypedValue
import androidx.core.widget.doAfterTextChanged
import com.google.android.material.textfield.TextInputEditText
import com.google.gson.JsonObject
import org.pakicek.monoforecast.domain.model.sdui.bool
import org.pakicek.monoforecast.domain.model.sdui.obj
import org.pakicek.monoforecast.domain.model.sdui.str
import org.pakicek.monoforecast.presentation.sdui.dialog.util.DialogUi

class InputTextItemRenderer : DialogItemRenderer {

    override val type: String = "input_text"

    override fun render(itemObj: JsonObject, ctx: DialogItemRenderContext) {
        val id = itemObj.str("id") ?: error("dialog item id is required")
        val data = itemObj.obj("data") ?: JsonObject()

        val hint = data.str("hint")
        val required = data.bool("required") ?: false
        val multiline = data.bool("multiline") ?: false

        if (required) ctx.requiredFields += id

        val til = DialogUi.outlinedTil(ctx.context).apply {
            this.hint = hint
            isErrorEnabled = true
        }

        val et = TextInputEditText(ctx.context).apply {
            layoutParams = DialogUi.lpMatchWrap()
            setTextSize(TypedValue.COMPLEX_UNIT_SP, ctx.inputTextSizeSp)
            if (ctx.typeface != null) typeface = ctx.typeface

            if (multiline) {
                inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE
                isSingleLine = false
                minLines = 1
                maxLines = 6
            } else {
                inputType = InputType.TYPE_CLASS_TEXT
                isSingleLine = true
                maxLines = 1
            }
        }

        til.addView(et)

        ctx.formValues[id] = ""
        ctx.textLayouts[id] = til

        et.doAfterTextChanged { s ->
            ctx.formValues[id] = s?.toString().orEmpty()
            til.error = null
        }

        ctx.addItem(til)
    }
}