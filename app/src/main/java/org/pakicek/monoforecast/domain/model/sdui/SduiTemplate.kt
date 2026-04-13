package org.pakicek.monoforecast.domain.model.sdui

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonNull
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import org.pakicek.monoforecast.domain.model.sdui.models.TemplateVars

object SduiTemplate {

    private val placeholderRegex = Regex("""\$\{([^}]+)\}""")
    private const val ESC = "___SDUI_ESC___"
    private const val ESCAPED_PREFIX = "\\${'$'}{"
    private const val UNESCAPED_PREFIX = "${'$'}{"

    fun resolve(element: JsonElement, vars: TemplateVars): JsonElement {
        return when {
            element.isJsonNull -> JsonNull.INSTANCE

            element.isJsonPrimitive -> {
                val p = element.asJsonPrimitive
                if (p.isString) JsonPrimitive(resolveString(p.asString, vars)) else element
            }

            element.isJsonArray -> {
                val out = JsonArray()
                element.asJsonArray.forEach { out.add(resolve(it, vars)) }
                out
            }

            element.isJsonObject -> {
                val out = JsonObject()
                element.asJsonObject.entrySet().forEach { (k, v) ->
                    out.add(k, resolve(v, vars))
                }
                out
            }

            else -> element
        }
    }

    fun resolveString(input: String, vars: TemplateVars): String {
        val pre = input.replace(ESCAPED_PREFIX, ESC)

        val replaced = placeholderRegex.replace(pre) { match ->
            eval(match.groupValues[1].trim(), vars)
        }

        return replaced.replace(ESC, UNESCAPED_PREFIX)
    }

    private fun eval(expr: String, vars: TemplateVars): String {
        return when {
            expr == "ts" -> vars.ts
            expr.startsWith("form.") -> {
                val key = expr.removePrefix("form.").trim()
                vars.form[key] ?: error("Template error: unknown form field '$key'")
            }
            else -> error("Template error: unknown expression '$expr'")
        }
    }
}