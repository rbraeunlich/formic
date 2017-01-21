package de.tu_berlin.formic.gatling.action

import io.gatling.commons.stats.OK
import io.gatling.commons.stats.KO
import io.gatling.core.session.Session
import io.gatling.core.stats.StatsEngine
import io.gatling.core.stats.message.ResponseTimings

/**
  * @author Ronny Bräunlich
  */

object FormicActions {
  def logOkTimingValues(start: Long, end: Long, session: Session, statsEngine: StatsEngine, name: String) = {
    val timings = ResponseTimings(start, end)
    statsEngine.logResponse(session, name, timings, OK, None, None)
  }

  def logKoTimingValues(start: Long, end: Long, session: Session, statsEngine: StatsEngine, name: String) = {
    val timings = ResponseTimings(start, end)
    statsEngine.logResponse(session, name, timings, KO, None, None)
  }
}






