package org.pakicek.monoforecast.presentation.sdui.engine.handlers

import org.pakicek.monoforecast.domain.model.sdui.SduiTemplate
import org.pakicek.monoforecast.domain.model.sdui.str
import org.pakicek.monoforecast.domain.model.sdui.models.SduiAction
import org.pakicek.monoforecast.domain.model.sdui.models.SduiEffect
import org.pakicek.monoforecast.domain.model.sdui.models.TemplateVars
import org.pakicek.monoforecast.presentation.sdui.engine.ActionHandler
import org.pakicek.monoforecast.presentation.sdui.engine.ActionResult
import org.pakicek.monoforecast.presentation.sdui.engine.SduiActionEngine

class NavigateActionHandler : ActionHandler {
    override val type: String = "navigate"

    override suspend fun handle(action: SduiAction, vars: TemplateVars, engine: SduiActionEngine): ActionResult {
        val pathT = requireNotNull(action.data?.str("path")) { "navigate.data.path is required" }
        val path = SduiTemplate.resolveString(pathT, vars)
        return ActionResult(effects = listOf(SduiEffect.Navigate(path)))
    }
}