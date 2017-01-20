package de.tu_berlin.formic.gatling.action

import de.tu_berlin.formic.common.DataTypeInstanceId
import io.gatling.core.action.Action
import io.gatling.core.session.Expression
import io.gatling.core.structure.ScenarioContext

/**
  * @author Ronny Bräunlich
  */
case class FormicLinearInsertActionBuilder(dataTypeInstanceId: DataTypeInstanceId, toInsert: Any, index: Expression[Int]) extends FormicActionBuilder {

  override def build(ctx: ScenarioContext, next: Action): Action = {
    val statsEngine = ctx.coreComponents.statsEngine
    val components = formicComponents(ctx)
    new LinearInsertion(dataTypeInstanceId, toInsert, index, statsEngine, next)
  }
}
