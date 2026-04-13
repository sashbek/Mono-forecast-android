package org.pakicek.monoforecast.presentation.sdui.engine.handlers

import org.pakicek.monoforecast.domain.model.sdui.SduiParser
import org.pakicek.monoforecast.domain.model.sdui.arr
import org.pakicek.monoforecast.domain.model.sdui.asObjOrNull
import org.pakicek.monoforecast.domain.model.sdui.models.SduiAction
import org.pakicek.monoforecast.domain.model.sdui.models.TemplateVars
import org.pakicek.monoforecast.presentation.sdui.engine.ActionHandler
import org.pakicek.monoforecast.presentation.sdui.engine.ActionResult
import org.pakicek.monoforecast.presentation.sdui.engine.SduiActionEngine

class BatchActionHandler : ActionHandler {
    override val type: String = "batch"

    override suspend fun handle(action: SduiAction, vars: TemplateVars, engine: SduiActionEngine): ActionResult {
        val arr = requireNotNull(action.data?.arr("actions")) { "batch.data.actions is required" }
        var didMutate = false
        val effects = mutableListOf<org.pakicek.monoforecast.domain.model.sdui.models.SduiEffect>()

        for (el in arr) {
            val obj = el.asObjOrNull() ?: continue
            val child = SduiParser.parseAction(obj)
            val res = engine.execute(child, vars)
            didMutate = didMutate || res.didMutate
            effects += res.effects
        }

        return ActionResult(effects = effects, didMutate = didMutate)
    }
}