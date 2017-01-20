package de.tu_berlin.formic.gatling.action

import akka.actor.ActorRef
import de.tu_berlin.formic.client.FormicSystem
import de.tu_berlin.formic.datatype.linear.client.FormicString
import io.gatling.commons.stats.OK
import io.gatling.commons.util.TimeHelper
import io.gatling.core.action.{Action, ChainableAction}
import io.gatling.core.session.Session
import io.gatling.core.stats.StatsEngine
import io.gatling.core.stats.message.ResponseTimings

/**
  * @author Ronny Bräunlich
  */
class CreateLinearDataType(formicSystem: FormicSystem, val statsEngine: StatsEngine, val next: Action) extends ChainableAction {

  override def name: String = "CreateDataType action"

  override def execute(session: Session): Unit = {
    val start = TimeHelper.nowMillis
    val string = new FormicString(() => {}, formicSystem)
    val end = TimeHelper.nowMillis
    session.set("formicString", string)
    logTimingValues(start, end, session)
    next ! session
  }

  def logTimingValues(start: Long, end: Long, session: Session) = {
    val timings = ResponseTimings(start, end)
    statsEngine.logResponse(session, name, timings, OK, None, None)
  }
}

class LinearInsertion(val next: ActorRef) {

}

class LinearDeletion(val next: ActorRef) {

}
