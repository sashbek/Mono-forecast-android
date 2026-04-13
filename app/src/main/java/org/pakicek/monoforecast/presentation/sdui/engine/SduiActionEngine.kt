package org.pakicek.monoforecast.presentation.sdui.engine

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonNull
import com.google.gson.JsonObject
import org.pakicek.monoforecast.domain.model.sdui.*
import org.pakicek.monoforecast.domain.model.sdui.models.SduiAction
import org.pakicek.monoforecast.domain.model.sdui.models.SduiEffect
import org.pakicek.monoforecast.domain.model.sdui.models.TemplateVars
import org.pakicek.monoforecast.domain.repository.EchoRepository

class SduiActionEngine(
    private val repo: EchoRepository
) {

    suspend fun execute(action: SduiAction, vars: TemplateVars): List<SduiEffect> {
        val effects = mutableListOf<SduiEffect>()

        when (action.type) {
            "navigate" -> {
                val path = requireNotNull(action.data?.str("path")) { "navigate.data.path is required" }
                effects += SduiEffect.Navigate(SduiTemplate.resolveString(path, vars))
            }

            "close_dialog" -> {
                effects += SduiEffect.CloseDialog
            }

            "batch" -> {
                val arr = requireNotNull(action.data?.arr("actions")) { "batch.data.actions is required" }
                for (el in arr) {
                    val aObj = el.asObjOrNull() ?: continue
                    effects += execute(SduiParser.parseAction(aObj), vars)
                }
            }

            "echo_put" -> {
                val pathT = requireNotNull(action.data?.str("path")) { "echo_put.data.path is required" }
                val path = SduiTemplate.resolveString(pathT, vars)

                val body = action.data.get("body") ?: JsonNull.INSTANCE
                val resolvedBody = SduiTemplate.resolve(body, vars)

                repo.put(path, resolvedBody).getOrThrow()
            }

            "echo_put_null" -> {
                val pathT = requireNotNull(action.data?.str("path")) { "echo_put_null.data.path is required" }
                val path = SduiTemplate.resolveString(pathT, vars)
                repo.put(path, JsonNull.INSTANCE).getOrThrow()
            }

            "echo_mutate_page" -> {
                val pathT = requireNotNull(action.data?.str("path")) { "echo_mutate_page.data.path is required" }
                val path = SduiTemplate.resolveString(pathT, vars)

                val mutationObj = requireNotNull(action.data.obj("mutation")) { "echo_mutate_page.data.mutation is required" }
                val mutationResolved = SduiTemplate.resolve(mutationObj, vars).asObjOrNull()
                    ?: error("mutation must be a JSON object")

                val currentPage = repo.get(path).getOrThrow().asObjOrNull()
                    ?: error("echo_mutate_page expects JSON object at $path")

                val updated = applyMutation(currentPage, mutationResolved)
                repo.put(path, updated).getOrThrow()
            }

            else -> effects += SduiEffect.Message("Unknown action: ${action.type}")
        }

        return effects
    }

    private fun applyMutation(page: JsonObject, mutation: JsonObject): JsonElement {
        val op = requireNotNull(mutation.str("op")) { "mutation.op is required" }
        val keepPinned = mutation.bool("keepPinnedBottomAtEnd") ?: false

        when (op) {
            "append" -> {
                val valueObj = mutation.get("value")?.asObjOrNull()
                    ?: error("append.value must be a JSON object")
                val blocks = page.arr("blocks") ?: JsonArray().also { page.add("blocks", it) }
                blocks.add(valueObj)
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

        if (keepPinned) {
            normalizePinnedBottom(page)
        }

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