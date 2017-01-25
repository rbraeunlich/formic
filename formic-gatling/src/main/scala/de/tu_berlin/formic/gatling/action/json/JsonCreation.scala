package de.tu_berlin.formic.gatling.action.json

import de.tu_berlin.formic.client.FormicSystem
import de.tu_berlin.formic.common.DataTypeInstanceId
import de.tu_berlin.formic.datatype.json.client.FormicJsonObject
import de.tu_berlin.formic.gatling.action.{FormicActions, SessionVariables, TimeMeasureCallback}
import io.gatling.commons.util.TimeHelper
import io.gatling.core.action.{Action, ChainableAction}
import io.gatling.core.session.{Expression, Session}
import io.gatling.core.stats.StatsEngine

/**
  * @author Ronny Bräunlich
  */
case class JsonCreation(dataTypeInstanceId: Expression[String], statsEngine: StatsEngine, next: Action) extends ChainableAction {

  override def name: String = "CreateJsonDataType action"

  override def execute(session: Session): Unit = {
    val start = TimeHelper.nowMillis
    val formicSystemOption = session(SessionVariables.FORMIC_SYSTEM).asOption[FormicSystem]
    val validatedDataTypeInstanceId = dataTypeInstanceId.apply(session)
    validatedDataTypeInstanceId.foreach { id =>
      formicSystemOption match {

        case Some(formicSystem) =>
          val json = new FormicJsonObject(session(SessionVariables.TIMEMEASURE_CALLBACK).as[TimeMeasureCallback].callbackMethod, formicSystem, DataTypeInstanceId.valueOf(id))
          val end = TimeHelper.nowMillis
          val modifiedSession = session.set(id, json)
          FormicActions.logOkTimingValues(start, end, session, statsEngine, name)
          next ! modifiedSession

        case None => throw new IllegalArgumentException("Users have to connect first!")
      }
    }
  }
}
