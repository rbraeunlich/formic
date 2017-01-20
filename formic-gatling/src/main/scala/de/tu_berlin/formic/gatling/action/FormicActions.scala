package de.tu_berlin.formic.gatling.action

import de.tu_berlin.formic.client.FormicSystem
import de.tu_berlin.formic.common.DataTypeInstanceId
import de.tu_berlin.formic.datatype.linear.client.{FormicList, FormicString}
import io.gatling.commons.stats.OK
import io.gatling.commons.util.TimeHelper
import io.gatling.core.action.{Action, ChainableAction}
import io.gatling.core.session.{Expression, Session}
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






