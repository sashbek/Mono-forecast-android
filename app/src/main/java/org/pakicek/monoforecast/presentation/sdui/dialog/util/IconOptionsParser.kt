package org.pakicek.monoforecast.presentation.sdui.dialog.util

import com.google.gson.JsonElement
import org.pakicek.monoforecast.domain.model.sdui.asArrOrNull
import org.pakicek.monoforecast.domain.model.sdui.str

object IconOptionsParser {

    fun parse(el: JsonElement): List<IconOption> {
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
}