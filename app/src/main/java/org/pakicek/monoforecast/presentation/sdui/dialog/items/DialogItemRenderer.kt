package org.pakicek.monoforecast.presentation.sdui.dialog.items

import com.google.gson.JsonObject

interface DialogItemRenderer {
    val type: String
    fun render(itemObj: JsonObject, ctx: DialogItemRenderContext)
}