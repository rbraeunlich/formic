package de.tu_berlin.formic.gatling.action

import akka.actor.ActorRef
import de.tu_berlin.formic.client.FormicSystem
import de.tu_berlin.formic.common.DataTypeInstanceId
import de.tu_berlin.formic.datatype.linear.client.{FormicList, FormicString}
import io.gatling.commons.stats.OK
import io.gatling.commons.util.TimeHelper
import io.gatling.core.action.{Action, ChainableAction}
import io.gatling.core.session.Session
import io.gatling.core.stats.StatsEngine
import io.gatling.core.stats.message.ResponseTimings

/**
  * @author Ronny Bräunlich
  */

object FormicActions {
  def logTimingValues(start: Long, end: Long, session: Session, statsEngine: StatsEngine, name: String) = {
    val timings = ResponseTimings(start, end)
    statsEngine.logResponse(session, name, timings, OK, None, None)
  }
}

class CreateLinearDataType(formicSystem: FormicSystem, val statsEngine: StatsEngine, val dataTypeInstanceId: DataTypeInstanceId, val next: Action) extends ChainableAction {

  override def name: String = "CreateDataType action"

  override def execute(session: Session): Unit = {
    val start = TimeHelper.nowMillis
    val string = new FormicString(() => {}, formicSystem, dataTypeInstanceId)
    val end = TimeHelper.nowMillis
    val modifiedSession = session.set(dataTypeInstanceId.id, string)
    FormicActions.logTimingValues(start, end, session, statsEngine, name)
    next ! modifiedSession
  }


}

class LinearInsertion(val dataTypeInstanceId: DataTypeInstanceId, val toInsert: Any, val index: Int, val statsEngine: StatsEngine, val next: Action) extends ChainableAction {

  override def name: String = "LinearInsert action"

  override def execute(session: Session): Unit = {
    val start = TimeHelper.nowMillis
    val dataTypeAttribute = session(dataTypeInstanceId.id)
    dataTypeAttribute.asOption[FormicList[Any]] match {
      case None => throw new IllegalArgumentException("Data type with id " + dataTypeInstanceId.id + " not found. Make to to create it first!")
      case Some(dataType) => dataType.add(index, toInsert)
    }
    val end = TimeHelper.nowMillis
    FormicActions.logTimingValues(start, end, session, statsEngine, name)
    next ! session
  }

}

class LinearDeletion(val next: ActorRef) {

}
