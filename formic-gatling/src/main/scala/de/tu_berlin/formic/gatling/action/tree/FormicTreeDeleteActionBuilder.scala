package de.tu_berlin.formic.gatling.action.tree

import de.tu_berlin.formic.gatling.action.FormicActionBuilder
import io.gatling.core.action.Action
import io.gatling.core.session._
import io.gatling.core.structure.ScenarioContext

/**
  * @author Ronny Bräunlich
  */
case class FormicTreeDeleteActionBuilder(dataTypeInstanceId: Expression[String], pathElements: Seq[Expression[Int]]) extends FormicActionBuilder {
  override def build(ctx: ScenarioContext, next: Action): Action = {
    val statsEngine = ctx.coreComponents.statsEngine
    TreeDeletion(dataTypeInstanceId, statsEngine, next, pathElements)
  }
}
