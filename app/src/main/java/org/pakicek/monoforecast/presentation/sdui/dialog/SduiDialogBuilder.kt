package org.pakicek.monoforecast.presentation.sdui.dialog

import android.content.Context
import android.text.InputType
import android.util.TypedValue
import android.view.ContextThemeWrapper
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.core.content.res.ResourcesCompat
import androidx.core.widget.NestedScrollView
import androidx.core.widget.doAfterTextChanged
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import org.pakicek.monoforecast.R
import org.pakicek.monoforecast.domain.model.sdui.*
import org.pakicek.monoforecast.domain.model.sdui.models.SduiAction
import org.pakicek.monoforecast.domain.model.sdui.models.TemplateVars

class SduiDialogBuilder(
    private val appContext: Context
) {

    data class BuiltDialog(
        val dialog: AlertDialog,
        val negativeAction: SduiAction?,
        val positiveAction: SduiAction?,
        val buildVarsOrNull: () -> TemplateVars?
    )

    fun build(dialogObj: JsonObject, baseVars: TemplateVars): BuiltDialog {
        val dialogContext = ContextThemeWrapper(appContext, R.style.ThemeOverlay_MonoForecast_MaterialAlertDialog)

        val typeface = ResourcesCompat.getFont(dialogContext, R.font.funnel_display)
        val inputTextSizeSp = 16f

        val title = dialogObj.str("title")
        val itemsArr = dialogObj.arr("items") ?: error("dialog.items is required")
        val buttonsArr = dialogObj.arr("buttons") ?: error("dialog.buttons is required")
        require(buttonsArr.size() >= 2) { "dialog.buttons must contain at least 2 items" }

        val formValues = mutableMapOf<String, String>()
        val requiredFields = mutableSetOf<String>()
        val textLayouts = mutableMapOf<String, TextInputLayout>()

        val content = LinearLayout(dialogContext).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = lpMatchWrap()
            setPadding(dp(dialogContext, 16), dp(dialogContext, 8), dp(dialogContext, 16), dp(dialogContext, 0))
        }

        fun addItem(view: View) {
            content.addView(view)
            content.addView(space(dialogContext, 12))
        }

        itemsArr.forEach { el ->
            val itemObj = el.asObjOrNull() ?: return@forEach
            val type = itemObj.str("type") ?: return@forEach
            val id = itemObj.str("id") ?: error("dialog item id is required")
            val data = itemObj.obj("data") ?: JsonObject()

            when (type) {
                "input_text" -> {
                    val hint = data.str("hint")
                    val required = data.bool("required") ?: false
                    val multiline = data.bool("multiline") ?: false

                    if (required) requiredFields += id

                    val til = newOutlinedTextInputLayout(dialogContext).apply {
                        this.hint = hint
                        isErrorEnabled = true
                    }

                    val et = TextInputEditText(dialogContext).apply {
                        layoutParams = lpMatchWrap()
                        setTextSize(TypedValue.COMPLEX_UNIT_SP, inputTextSizeSp)
                        if (typeface != null) setTypeface(typeface)

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

                    formValues[id] = ""
                    textLayouts[id] = til

                    et.doAfterTextChanged { s ->
                        formValues[id] = s?.toString().orEmpty()
                        til.error = null
                    }

                    addItem(til)
                }

                "icon_select" -> {
                    val label = data.str("label") ?: "Icon"
                    val optionsEl = requireNotNull(data.get("options")) { "icon_select.options is required" }
                    val options = normalizeIconOptions(optionsEl)
                    require(options.isNotEmpty()) { "icon_select.options must not be empty" }

                    val til = newOutlinedTextInputLayout(dialogContext).apply {
                        hint = label
                        endIconMode = TextInputLayout.END_ICON_DROPDOWN_MENU
                        isErrorEnabled = false
                    }

                    val labels = options.map { it.label }

                    val adapter = NoFilterArrayAdapter(
                        dialogContext,
                        com.google.android.material.R.layout.mtrl_auto_complete_simple_item,
                        labels
                    )

                    val actv = MaterialAutoCompleteTextView(dialogContext).apply {
                        layoutParams = lpMatchWrap()

                        setAdapter(adapter)
                        setText(labels.first(), false)

                        keyListener = null
                        inputType = InputType.TYPE_NULL
                        isCursorVisible = false
                        setTextIsSelectable(false)
                        setOnLongClickListener { true }

                        setOnClickListener { showDropDown() }
                    }

                    til.addView(actv)

                    formValues[id] = options.first().key

                    til.setEndIconOnClickListener {
                        actv.requestFocus()
                        actv.showDropDown()
                    }

                    actv.setOnItemClickListener { _, _, position, _ ->
                        formValues[id] = options.getOrNull(position)?.key.orEmpty()
                    }

                    addItem(til)
                }

                else -> Unit
            }
        }

        val negative = buttonsArr[0].asObjOrNull() ?: error("dialog.buttons[0] must be object")
        val positive = buttonsArr[1].asObjOrNull() ?: error("dialog.buttons[1] must be object")

        val negData = negative.obj("data") ?: JsonObject()
        val posData = positive.obj("data") ?: JsonObject()

        val negText = negData.str("text") ?: "Cancel"
        val posText = posData.str("text") ?: "OK"

        val negAction = negData.obj("action")?.let { SduiParser.parseAction(it) }
        val posAction = posData.obj("action")?.let { SduiParser.parseAction(it) }

        val scroll = NestedScrollView(dialogContext).apply {
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            addView(content)
        }

        val dialog = MaterialAlertDialogBuilder(dialogContext, R.style.ThemeOverlay_MonoForecast_MaterialAlertDialog)
            .setTitle(title)
            .setView(scroll)
            .setNegativeButton(negText, null)
            .setPositiveButton(posText, null)
            .create()

        val buildVarsOrNull = {
            var ok = true
            requiredFields.forEach { fieldId ->
                val value = formValues[fieldId].orEmpty().trim()
                if (value.isBlank()) {
                    ok = false
                    textLayouts[fieldId]?.error = appContext.getString(R.string.bdui_fill_fields)
                }
            }
            if (!ok) null else TemplateVars(ts = baseVars.ts, form = formValues.mapValues { it.value })
        }

        return BuiltDialog(
            dialog = dialog,
            negativeAction = negAction,
            positiveAction = posAction,
            buildVarsOrNull = buildVarsOrNull
        )
    }

    private data class IconOption(val key: String, val label: String)

    private fun normalizeIconOptions(el: JsonElement): List<IconOption> {
        val arr = el.asArrOrNull() ?: error("icon_select.options must be array")
        return arr.mapNotNull { item ->
            when {
                item.isJsonPrimitive -> {
                    val s = item.asString
                    IconOption(key = s, label = s)
                }
                item.isJsonObject -> {
                    val o = item.asJsonObject
                    val key = o.str("key") ?: return@mapNotNull null
                    val label = o.str("label") ?: key
                    IconOption(key = key, label = label)
                }
                else -> null
            }
        }
    }

    private fun newOutlinedTextInputLayout(ctx: Context): TextInputLayout {
        return TextInputLayout(ctx).apply {
            layoutParams = lpMatchWrap()
            boxBackgroundMode = TextInputLayout.BOX_BACKGROUND_OUTLINE
        }
    }

    private fun lpMatchWrap(): ViewGroup.LayoutParams =
        LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

    private fun dp(ctx: Context, v: Int): Int =
        (v * ctx.resources.displayMetrics.density).toInt()

    private fun space(ctx: Context, dp: Int): View =
        View(ctx).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                this@SduiDialogBuilder.dp(ctx, dp)
            )
        }

    private class NoFilterArrayAdapter(
        context: Context,
        layoutRes: Int,
        private val allItems: List<String>
    ) : android.widget.ArrayAdapter<String>(context, layoutRes, ArrayList(allItems)) {

        private val noFilter = object : android.widget.Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                return FilterResults().apply {
                    values = allItems
                    count = allItems.size
                }
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                clear()
                addAll(allItems)
                notifyDataSetChanged()
            }

            override fun convertResultToString(resultValue: Any): CharSequence {
                return resultValue.toString()
            }
        }

        override fun getFilter(): android.widget.Filter = noFilter
    }
}