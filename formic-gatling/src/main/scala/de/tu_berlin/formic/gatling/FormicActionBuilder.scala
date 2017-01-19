package de.tu_berlin.formic.gatling

import io.gatling.core.action.Action
import io.gatling.core.action.builder.ActionBuilder
import io.gatling.core.structure.ScenarioContext

/**
  * @author Ronny Bräunlich
  */
class FormicActionBuilder extends ActionBuilder {

  private def formicComponents(ctx: ScenarioContext): FormicGatlingComponents = {
    ctx.protocolComponentsRegistry.components(FormicGatlingProtocol.FormicGatlingProtocolKey).asInstanceOf[FormicGatlingComponents]
  }

  override def build(ctx: ScenarioContext, next: Action): Action = {
    val statsEngine = ctx.coreComponents.statsEngine
    val components = formicComponents(ctx)
    new CreateDataType(components.formicGatlingProtocol.formicSystem, next)
  }
}

object FormicActionBuilder {
  def apply(): FormicActionBuilder = new FormicActionBuilder()
}