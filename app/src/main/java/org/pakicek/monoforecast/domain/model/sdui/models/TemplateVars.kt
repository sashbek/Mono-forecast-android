package org.pakicek.monoforecast.domain.model.sdui.models

data class TemplateVars(
    val ts: String,
    val form: Map<String, String> = emptyMap()
)