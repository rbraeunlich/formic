package de.tu_berlin.formic.gatling.action.linear

import de.tu_berlin.formic.common.DataStructureInstanceId$
import de.tu_berlin.formic.gatling.action.FormicActionBuilder
import io.gatling.core.action.Action
import io.gatling.core.session.Expression
import io.gatling.core.structure.ScenarioContext

/**
  * @author Ronny Bräunlich
  */
case class FormicLinearDeleteActionBuilder(dataTypeInstanceId: Expression[String], index: Expression[Int]) extends FormicActionBuilder {

  override def build(ctx: ScenarioContext, next: Action): Action = {
    val statsEngine = ctx.coreComponents.statsEngine
    LinearDeletion(dataTypeInstanceId, index, statsEngine, next)
  }
}
