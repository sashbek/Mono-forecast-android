package org.pakicek.monoforecast.domain.model.sdui

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject

fun JsonObject.str(name: String): String? = get(name)?.takeIf { it.isJsonPrimitive }?.asString
fun JsonObject.bool(name: String): Boolean? = get(name)?.takeIf { it.isJsonPrimitive }?.asBoolean
fun JsonObject.obj(name: String): JsonObject? = get(name)?.takeIf { it.isJsonObject }?.asJsonObject
fun JsonObject.arr(name: String): JsonArray? = get(name)?.takeIf { it.isJsonArray }?.asJsonArray

fun JsonElement.asObjOrNull(): JsonObject? = if (isJsonObject) asJsonObject else null
fun JsonElement.asArrOrNull(): JsonArray? = if (isJsonArray) asJsonArray else null