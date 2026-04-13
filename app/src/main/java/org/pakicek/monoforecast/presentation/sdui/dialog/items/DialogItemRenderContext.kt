package org.pakicek.monoforecast.presentation.sdui.dialog.items

import android.content.Context
import android.graphics.Typeface
import android.view.View
import com.google.android.material.textfield.TextInputLayout

data class DialogItemRenderContext(
    val context: Context,
    val typeface: Typeface?,
    val inputTextSizeSp: Float,
    val addItem: (View) -> Unit,
    val formValues: MutableMap<String, String>,
    val requiredFields: MutableSet<String>,
    val textLayouts: MutableMap<String, TextInputLayout>
)