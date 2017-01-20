package de.tu_berlin.formic.gatling.action

import de.tu_berlin.formic.common.DataTypeInstanceId
import io.gatling.core.action.Action
import io.gatling.core.structure.ScenarioContext

/**
  * @author Ronny Bräunlich
  */
case class FormicCreateActionBuilder(dataTypeInstanceId: DataTypeInstanceId, dataType: String) extends FormicActionBuilder {

  override def build(ctx: ScenarioContext, next: Action): Action = {
    val statsEngine = ctx.coreComponents.statsEngine
    val components = formicComponents(ctx)

    dataType match {
      case "linear" => new CreateLinearDataType(components.formicGatlingProtocol.formicSystem, statsEngine, next)
    }
  }

}
