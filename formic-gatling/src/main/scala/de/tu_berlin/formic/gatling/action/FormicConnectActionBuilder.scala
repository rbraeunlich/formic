package de.tu_berlin.formic.gatling.action

import io.gatling.core.action.Action
import io.gatling.core.structure.ScenarioContext

/**
  * @author Ronny Bräunlich
  */
case class FormicConnectActionBuilder() extends FormicActionBuilder {

  override def build(ctx: ScenarioContext, next: Action): Action = {
    val statsEngine = ctx.coreComponents.statsEngine
    val components = formicComponents(ctx)
    val protocol = components.formicGatlingProtocol
    FormicConnectAction(protocol.config, statsEngine, next)
  }

}
