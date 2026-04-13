package org.pakicek.monoforecast.presentation.sdui.dialog

import android.content.Context
import android.view.ContextThemeWrapper
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.core.content.res.ResourcesCompat
import androidx.core.widget.NestedScrollView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.JsonObject
import org.pakicek.monoforecast.R
import org.pakicek.monoforecast.domain.model.sdui.SduiParser
import org.pakicek.monoforecast.domain.model.sdui.arr
import org.pakicek.monoforecast.domain.model.sdui.asObjOrNull
import org.pakicek.monoforecast.domain.model.sdui.obj
import org.pakicek.monoforecast.domain.model.sdui.str
import org.pakicek.monoforecast.domain.model.sdui.models.SduiAction
import org.pakicek.monoforecast.domain.model.sdui.models.TemplateVars
import org.pakicek.monoforecast.presentation.sdui.dialog.items.DialogItemRegistry
import org.pakicek.monoforecast.presentation.sdui.dialog.items.DialogItemRenderContext
import org.pakicek.monoforecast.presentation.sdui.dialog.items.IconSelectItemRenderer
import org.pakicek.monoforecast.presentation.sdui.dialog.items.InputTextItemRenderer
import org.pakicek.monoforecast.presentation.sdui.dialog.util.DialogUi

class SduiDialogBuilder(
    private val appContext: Context
) {

    data class BuiltDialog(
        val dialog: AlertDialog,
        val negativeAction: SduiAction?,
        val positiveAction: SduiAction?,
        val buildVarsOrNull: () -> TemplateVars?
    )

    private val registry = DialogItemRegistry(
        listOf(
            InputTextItemRenderer(),
            IconSelectItemRenderer()
        )
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
        val textLayouts = mutableMapOf<String, com.google.android.material.textfield.TextInputLayout>()

        val content = LinearLayout(dialogContext).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = DialogUi.lpMatchWrap()
            setPadding(DialogUi.dp(dialogContext, 16), DialogUi.dp(dialogContext, 8), DialogUi.dp(dialogContext, 16), 0)
        }

        fun addItem(view: View) {
            content.addView(view)
            content.addView(DialogUi.space(dialogContext, 12))
        }

        val ctx = DialogItemRenderContext(
            context = dialogContext,
            typeface = typeface,
            inputTextSizeSp = inputTextSizeSp,
            addItem = ::addItem,
            formValues = formValues,
            requiredFields = requiredFields,
            textLayouts = textLayouts
        )

        itemsArr.forEach { el ->
            val itemObj = el.asObjOrNull() ?: return@forEach
            val type = itemObj.str("type") ?: return@forEach
            registry.get(type)?.render(itemObj, ctx)
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
}