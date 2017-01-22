package de.tu_berlin.formic.gatling.action.json

import de.tu_berlin.formic.gatling.action.FormicActionBuilder
import io.gatling.core.action.Action
import io.gatling.core.session._
import io.gatling.core.structure.ScenarioContext
import upickle.default._
/**
  * @author Ronny Bräunlich
  */
case class FormicJsonInsertActionBuilder[T](dataTypeInstanceId: Expression[String], toInsert: T, pathElements: Seq[Expression[String]])(implicit writer: Writer[T]) extends FormicActionBuilder {
  override def build(ctx: ScenarioContext, next: Action): Action = {
    val statsEngine = ctx.coreComponents.statsEngine
    JsonInsertion(dataTypeInstanceId, statsEngine, toInsert, next, pathElements)
  }
}
