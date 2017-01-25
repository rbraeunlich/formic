package de.tu_berlin.formic.gatling.action.json

import de.tu_berlin.formic.datatype.json.JsonPath
import de.tu_berlin.formic.datatype.json.client.FormicJsonObject
import de.tu_berlin.formic.gatling.action.{SessionVariables, TimeMeasureCallback}
import io.gatling.commons.util.TimeHelper
import io.gatling.core.action.{Action, ChainableAction}
import io.gatling.core.session.{Expression, Session}
import io.gatling.core.stats.StatsEngine
import upickle.default._

/**
  * @author Ronny Bräunlich
  */
case class JsonInsertion[T](dataTypeInstanceId: Expression[String], statsEngine: StatsEngine, toInsert: T, next: Action, pathElements: Seq[Expression[String]])(implicit writer: Writer[T]) extends ChainableAction {

  override def name: String = "JsonInsert action"

  override def execute(session: Session): Unit = {
    val start = TimeHelper.nowMillis
    dataTypeInstanceId.apply(session).foreach { id =>
      val dataTypeAttribute = session(id)
      val validatedPath = pathElements.map(elem => elem.apply(session).get)
      val path = JsonPath(validatedPath: _*)
      dataTypeAttribute.asOption[FormicJsonObject] match {
        case None => throw new IllegalArgumentException("Data type not found. Create it first!")
        case Some(dataType) =>
          val opId = toInsert match {
            case d: Double => dataType.insert(d, path)
            case s: String => dataType.insert(s, path)
            case c: Char => dataType.insert(c, path)
            case b: Boolean => dataType.insert(b, path)
            case _ => dataType.insert(toInsert, path)
          }
          session(SessionVariables.TIMEMEASURE_CALLBACK).as[TimeMeasureCallback]
            .addListener(TimeMeasureCallback.RemoteOperationTimeMeasureListener(opId, start, session, statsEngine, name))
      }
      next ! session
    }
  }
}
