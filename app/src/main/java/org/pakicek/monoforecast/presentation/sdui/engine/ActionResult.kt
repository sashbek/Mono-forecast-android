package org.pakicek.monoforecast.presentation.sdui.engine

import org.pakicek.monoforecast.domain.model.sdui.models.SduiEffect

data class ActionResult(
    val effects: List<SduiEffect> = emptyList(),
    val didMutate: Boolean = false
)