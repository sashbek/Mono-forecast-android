package org.pakicek.monoforecast.presentation.sdui.dialog.items

class DialogItemRegistry(
    renderers: List<DialogItemRenderer>
) {
    private val map = renderers.associateBy { it.type }
    fun get(type: String): DialogItemRenderer? = map[type]
}