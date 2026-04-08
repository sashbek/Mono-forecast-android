package org.pakicek.monoforecast.domain.model.bdui

data class BduiPage(
    val type: String = "page",
    val title: String = "",
    val blocks: List<BduiBlock> = emptyList()
)