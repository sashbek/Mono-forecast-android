package org.pakicek.monoforecast.presentation.sdui.dialog.items

import android.text.InputType
import android.util.TypedValue
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputLayout
import com.google.gson.JsonObject
import org.pakicek.monoforecast.domain.model.sdui.obj
import org.pakicek.monoforecast.domain.model.sdui.str
import org.pakicek.monoforecast.presentation.sdui.dialog.util.DialogUi
import org.pakicek.monoforecast.presentation.sdui.dialog.util.IconOptionsParser
import org.pakicek.monoforecast.presentation.sdui.dialog.util.NoFilterArrayAdapter

class IconSelectItemRenderer : DialogItemRenderer {

    override val type: String = "icon_select"

    override fun render(itemObj: JsonObject, ctx: DialogItemRenderContext) {
        val id = itemObj.str("id") ?: error("dialog item id is required")
        val data = itemObj.obj("data") ?: JsonObject()

        val label = data.str("label") ?: "Icon"
        val optionsEl = requireNotNull(data.get("options")) { "icon_select.options is required" }
        val options = IconOptionsParser.parse(optionsEl)
        require(options.isNotEmpty()) { "icon_select.options must not be empty" }

        val labels = options.map { it.label }

        val til = DialogUi.outlinedTil(ctx.context).apply {
            hint = label
            endIconMode = TextInputLayout.END_ICON_DROPDOWN_MENU
            isErrorEnabled = false
        }

        val adapter = NoFilterArrayAdapter(
            ctx.context,
            com.google.android.material.R.layout.mtrl_auto_complete_simple_item,
            labels
        )

        val actv = MaterialAutoCompleteTextView(ctx.context).apply {
            layoutParams = DialogUi.lpMatchWrap()
            setAdapter(adapter)
            threshold = 0

            setTextSize(TypedValue.COMPLEX_UNIT_SP, ctx.inputTextSizeSp)
            if (ctx.typeface != null) typeface = ctx.typeface

            keyListener = null
            inputType = InputType.TYPE_NULL
            isCursorVisible = false
            setTextIsSelectable(false)
            setOnLongClickListener { true }

            setText(labels.first(), false)
            setOnClickListener { showDropDown() }
        }

        til.addView(actv)

        ctx.formValues[id] = options.first().key

        til.setEndIconOnClickListener {
            actv.requestFocus()
            actv.showDropDown()
        }

        actv.setOnItemClickListener { _, _, position, _ ->
            ctx.formValues[id] = options.getOrNull(position)?.key.orEmpty()
        }

        ctx.addItem(til)
    }
}