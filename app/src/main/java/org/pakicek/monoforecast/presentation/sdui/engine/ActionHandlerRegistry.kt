package org.pakicek.monoforecast.presentation.sdui.engine

class ActionHandlerRegistry(
    handlers: List<ActionHandler>,
    private val unknown: ActionHandler
) {
    private val map = handlers.associateBy { it.type }
    fun get(type: String): ActionHandler = map[type] ?: unknown
}