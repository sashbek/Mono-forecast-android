package org.pakicek.monoforecast.domain.model.sdui

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import org.pakicek.monoforecast.domain.model.sdui.models.SduiAction
import org.pakicek.monoforecast.domain.model.sdui.models.SduiBlock
import org.pakicek.monoforecast.domain.model.sdui.models.SduiLayout
import org.pakicek.monoforecast.domain.model.sdui.models.SduiPage

object SduiParser {

    fun parsePage(root: JsonElement): SduiPage {
        val obj = root.asObjOrNull() ?: error("Page must be a JSON object")
        val blocksArr = obj.arr("blocks") ?: error("Page.blocks must be an array")

        val blocks = blocksArr.mapNotNull { el ->
            el.asObjOrNull()?.let { parseBlock(it) }
        }

        return SduiPage(
            title = obj.str("title"),
            blocks = blocks
        )
    }

    fun parseBlock(obj: JsonObject): SduiBlock {
        val type = obj.str("type") ?: error("Block.type is required")
        val id = obj.str("id")
        val layout = obj.obj("layout")?.let { SduiLayout(pin = it.str("pin")) }
        val data = obj.obj("data")

        return SduiBlock(
            type = type,
            id = id,
            layout = layout,
            data = data
        )
    }

    fun parseAction(obj: JsonObject): SduiAction {
        val type = obj.str("type") ?: error("Action.type is required")
        val data = obj.obj("data")
        return SduiAction(type = type, data = data)
    }
}