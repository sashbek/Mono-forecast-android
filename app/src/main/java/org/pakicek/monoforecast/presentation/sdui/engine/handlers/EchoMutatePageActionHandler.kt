package org.pakicek.monoforecast.presentation.sdui.engine.handlers

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import org.pakicek.monoforecast.domain.model.sdui.*
import org.pakicek.monoforecast.domain.model.sdui.models.SduiAction
import org.pakicek.monoforecast.domain.model.sdui.models.TemplateVars
import org.pakicek.monoforecast.presentation.sdui.engine.ActionHandler
import org.pakicek.monoforecast.presentation.sdui.engine.ActionResult
import org.pakicek.monoforecast.presentation.sdui.engine.SduiActionEngine

class EchoMutatePageActionHandler : ActionHandler {
    override val type: String = "echo_mutate_page"

    override suspend fun handle(action: SduiAction, vars: TemplateVars, engine: SduiActionEngine): ActionResult {
        val pathT = requireNotNull(action.data?.str("path")) { "echo_mutate_page.data.path is required" }
        val path = SduiTemplate.resolveString(pathT, vars)

        val mutationObj = requireNotNull(action.data.obj("mutation")) { "echo_mutate_page.data.mutation is required" }
        val mutation = SduiTemplate.resolve(mutationObj, vars).asObjOrNull() ?: error("mutation must be object")

        val current = engine.repo.get(path).getOrThrow().asObjOrNull() ?: error("page must be object at $path")
        val updated = applyMutation(current, mutation)
        engine.repo.put(path, updated).getOrThrow()

        return ActionResult(didMutate = true)
    }

    private fun applyMutation(page: JsonObject, mutation: JsonObject): JsonElement {
        val op = requireNotNull(mutation.str("op")) { "mutation.op is required" }
        val keepPinned = mutation.bool("keepPinnedBottomAtEnd") ?: false

        when (op) {
            "append" -> {
                val value = mutation.get("value")?.asObjOrNull() ?: error("append.value must be object")
                val blocks = page.arr("blocks") ?: JsonArray().also { page.add("blocks", it) }
                blocks.add(value)
            }

            "remove_where" -> {
                val where = requireNotNull(mutation.obj("where")) { "remove_where.where is required" }
                val type = where.str("type")
                val id = where.str("id")
                require(type != null || id != null) { "remove_where.where must contain type or id" }

                val blocks = page.arr("blocks") ?: JsonArray()
                val out = JsonArray()
                blocks.forEach { el ->
                    val obj = el.asObjOrNull()
                    val matches = obj != null && matchesWhere(obj, type, id)
                    if (!matches) out.add(el)
                }
                page.add("blocks", out)
            }

            else -> error("Unknown mutation.op: $op")
        }

        if (keepPinned) normalizePinnedBottom(page)
        return page
    }

    private fun matchesWhere(block: JsonObject, type: String?, id: String?): Boolean {
        val typeOk = type?.let { it == block.str("type") } ?: true
        val idOk = id?.let { it == block.str("id") } ?: true
        return typeOk && idOk
    }

    private fun normalizePinnedBottom(page: JsonObject) {
        val blocks = page.arr("blocks") ?: return
        val top = JsonArray()
        val bottom = JsonArray()

        blocks.forEach { el ->
            val obj = el.asObjOrNull()
            val pin = obj?.obj("layout")?.str("pin")
            if (pin == "bottom") bottom.add(el) else top.add(el)
        }

        val merged = JsonArray().apply {
            top.forEach { add(it) }
            bottom.forEach { add(it) }
        }
        page.add("blocks", merged)
    }
}