package org.pakicek.monoforecast.presentation.sdui.engine.handlers

import org.pakicek.monoforecast.domain.model.sdui.obj
import org.pakicek.monoforecast.domain.model.sdui.models.SduiAction
import org.pakicek.monoforecast.domain.model.sdui.models.SduiEffect
import org.pakicek.monoforecast.domain.model.sdui.models.TemplateVars
import org.pakicek.monoforecast.presentation.sdui.engine.ActionHandler
import org.pakicek.monoforecast.presentation.sdui.engine.ActionResult
import org.pakicek.monoforecast.presentation.sdui.engine.SduiActionEngine

class OpenDialogActionHandler : ActionHandler {
    override val type: String = "open_dialog"

    override suspend fun handle(action: SduiAction, vars: TemplateVars, engine: SduiActionEngine): ActionResult {
        val dialog = requireNotNull(action.data?.obj("dialog")) { "open_dialog.data.dialog is required" }
        val baseVars = TemplateVars(ts = System.currentTimeMillis().toString(), form = emptyMap())
        return ActionResult(effects = listOf(SduiEffect.OpenDialog(dialog, baseVars)))
    }
}