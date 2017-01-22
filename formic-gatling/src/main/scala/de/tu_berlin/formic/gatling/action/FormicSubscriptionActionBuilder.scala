package de.tu_berlin.formic.gatling.action

import io.gatling.core.action.Action
import io.gatling.core.session.Expression
import io.gatling.core.structure.ScenarioContext

/**
  * @author Ronny Bräunlich
  */
case class FormicSubscriptionActionBuilder(dataTypeInstanceId: Expression[String]) extends FormicActionBuilder {

  override def build(ctx: ScenarioContext, next: Action): Action = {
    val statsEngine = ctx.coreComponents.statsEngine
    SubscriptionAction(dataTypeInstanceId, statsEngine, next)
  }
}
