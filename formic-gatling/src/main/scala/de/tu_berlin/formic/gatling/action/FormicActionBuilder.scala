package de.tu_berlin.formic.gatling.action

import de.tu_berlin.formic.gatling.FormicGatlingComponents
import de.tu_berlin.formic.gatling.protocol.FormicGatlingProtocol
import io.gatling.core.action.builder.ActionBuilder
import io.gatling.core.structure.ScenarioContext

/**
  * @author Ronny Bräunlich
  */
abstract class FormicActionBuilder extends ActionBuilder {

  protected def formicComponents(ctx: ScenarioContext): FormicGatlingComponents = {
    ctx.protocolComponentsRegistry.components(FormicGatlingProtocol.FormicGatlingProtocolKey).asInstanceOf[FormicGatlingComponents]
  }

}