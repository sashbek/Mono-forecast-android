package org.pakicek.monoforecast.presentation.sdui.engine

import org.pakicek.monoforecast.domain.model.sdui.models.SduiAction
import org.pakicek.monoforecast.domain.model.sdui.models.TemplateVars
import org.pakicek.monoforecast.domain.repository.EchoRepository
import org.pakicek.monoforecast.presentation.sdui.engine.handlers.*

class SduiActionEngine(
    val repo: EchoRepository
) {
    private val registry = ActionHandlerRegistry(
        handlers = listOf(
            NavigateActionHandler(),
            CloseDialogActionHandler(),
            OpenDialogActionHandler(),
            BatchActionHandler(),
            EchoPutActionHandler(),
            EchoPutNullActionHandler(),
            EchoMutatePageActionHandler()
        ),
        unknown = UnknownActionHandler()
    )

    suspend fun execute(action: SduiAction, vars: TemplateVars): ActionResult {
        return registry.get(action.type).handle(action, vars, this)
    }
}