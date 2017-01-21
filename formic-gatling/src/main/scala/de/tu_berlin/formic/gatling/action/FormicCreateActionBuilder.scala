package de.tu_berlin.formic.gatling.action

import de.tu_berlin.formic.gatling.action.linear.LinearCreation
import io.gatling.core.action.Action
import io.gatling.core.structure.ScenarioContext

/**
  * @author Ronny Bräunlich
  */
case class FormicCreateActionBuilder(dataType: String) extends FormicActionBuilder {

  override def build(ctx: ScenarioContext, next: Action): Action = {
    val statsEngine = ctx.coreComponents.statsEngine
    dataType match {
      case "linear" => LinearCreation(statsEngine, next)
    }
  }

}
