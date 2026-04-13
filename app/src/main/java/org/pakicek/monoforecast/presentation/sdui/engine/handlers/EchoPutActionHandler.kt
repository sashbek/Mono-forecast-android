package org.pakicek.monoforecast.presentation.sdui.engine.handlers

import com.google.gson.JsonNull
import org.pakicek.monoforecast.domain.model.sdui.SduiTemplate
import org.pakicek.monoforecast.domain.model.sdui.str
import org.pakicek.monoforecast.domain.model.sdui.models.SduiAction
import org.pakicek.monoforecast.domain.model.sdui.models.TemplateVars
import org.pakicek.monoforecast.presentation.sdui.engine.ActionHandler
import org.pakicek.monoforecast.presentation.sdui.engine.ActionResult
import org.pakicek.monoforecast.presentation.sdui.engine.SduiActionEngine

class EchoPutActionHandler : ActionHandler {
    override val type: String = "echo_put"

    override suspend fun handle(action: SduiAction, vars: TemplateVars, engine: SduiActionEngine): ActionResult {
        val pathT = requireNotNull(action.data?.str("path")) { "echo_put.data.path is required" }
        val path = SduiTemplate.resolveString(pathT, vars)

        val body = action.data.get("body") ?: JsonNull.INSTANCE
        val resolved = SduiTemplate.resolve(body, vars)

        engine.repo.put(path, resolved).getOrThrow()
        return ActionResult(didMutate = true)
    }
}