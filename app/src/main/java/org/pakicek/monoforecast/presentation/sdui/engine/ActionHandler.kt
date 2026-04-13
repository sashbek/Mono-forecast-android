package org.pakicek.monoforecast.presentation.sdui.engine

import org.pakicek.monoforecast.domain.model.sdui.models.SduiAction
import org.pakicek.monoforecast.domain.model.sdui.models.TemplateVars

interface ActionHandler {
    val type: String
    suspend fun handle(action: SduiAction, vars: TemplateVars, engine: SduiActionEngine): ActionResult
}