package de.tu_berlin.formic.gatling.action.linear

import de.tu_berlin.formic.common.DataTypeInstanceId
import de.tu_berlin.formic.gatling.action.FormicActionBuilder
import io.gatling.core.action.Action
import io.gatling.core.session.Expression
import io.gatling.core.structure.ScenarioContext

/**
  * @author Ronny Bräunlich
  */
case class FormicLinearDeleteActionBuilder(dataTypeInstanceId: DataTypeInstanceId, index: Expression[Int]) extends FormicActionBuilder {

  override def build(ctx: ScenarioContext, next: Action): Action = {
    val statsEngine = ctx.coreComponents.statsEngine
    new LinearDeletion(dataTypeInstanceId, index, statsEngine, next)
  }
}
