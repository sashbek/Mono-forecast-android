package org.pakicek.monoforecast.domain.model.bdui

data class BduiBlock(
    val type: String,
    val id: String? = null,
    val title: String? = null,
    val text: String? = null,
    val imageKey: String? = null,
    val path: String? = null,
    val buttonText: String? = null,
    val action: String? = null
)