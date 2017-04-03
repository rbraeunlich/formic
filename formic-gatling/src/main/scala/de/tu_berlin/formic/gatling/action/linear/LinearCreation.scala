package de.tu_berlin.formic.gatling.action.linear

import de.tu_berlin.formic.client.FormicSystem
import de.tu_berlin.formic.common.DataStructureInstanceId
import de.tu_berlin.formic.datastructure.linear.client.FormicString
import de.tu_berlin.formic.gatling.action.TimeMeasureCallback.CreateResponseTimeMeasureListener
import de.tu_berlin.formic.gatling.action.{SessionVariables, TimeMeasureCallback}
import io.gatling.commons.util.TimeHelper
import io.gatling.core.action.{Action, ChainableAction}
import io.gatling.core.session.{Expression, Session}
import io.gatling.core.stats.StatsEngine

/**
  * @author Ronny Bräunlich
  */
case class LinearCreation(dataTypeInstanceId: Expression[String], statsEngine: StatsEngine, next: Action) extends ChainableAction {

  override def name: String = "CreateLinearDataType action"

  override def execute(session: Session): Unit = {
    val start = TimeHelper.nowMillis
    val formicSystemOption = session(SessionVariables.FORMIC_SYSTEM).asOption[FormicSystem]
    val validatedDataTypeInstanceId = dataTypeInstanceId.apply(session)
    validatedDataTypeInstanceId.foreach { id =>
      formicSystemOption match {

        case Some(formicSystem) =>
          val callback = session(SessionVariables.TIMEMEASURE_CALLBACK).as[TimeMeasureCallback]
          val string = new FormicString(callback.callbackMethod, formicSystem, DataStructureInstanceId.valueOf(id))
          val modifiedSession = session.set(id, string)
          callback.addListener(CreateResponseTimeMeasureListener(string.dataStructureInstanceId, start, session, statsEngine, name))
          next ! modifiedSession

        case None => throw new IllegalArgumentException("Users have to connect first!")
      }
    }
  }
}
