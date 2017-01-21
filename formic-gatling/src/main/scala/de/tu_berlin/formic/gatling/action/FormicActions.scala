package de.tu_berlin.formic.gatling.action

import io.gatling.commons.stats.OK
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






