package de.tu_berlin.formic.gatling.action.linear

import de.tu_berlin.formic.client.FormicSystem
import de.tu_berlin.formic.common.DataTypeInstanceId
import de.tu_berlin.formic.datatype.linear.client.FormicString
import de.tu_berlin.formic.gatling.action.{FormicActions, SessionVariables}
import io.gatling.commons.util.TimeHelper
import io.gatling.core.action.{Action, ChainableAction}
import io.gatling.core.session.Session
import io.gatling.core.stats.StatsEngine

/**
  * @author Ronny Bräunlich
  */
case class CreateLinearDataType(statsEngine: StatsEngine, next: Action) extends ChainableAction {

  override def name: String = "CreateDataType action"

  override def execute(session: Session): Unit = {
    val start = TimeHelper.nowMillis
    val formicSystemOption = session(SessionVariables.FORMIC_SYSTEM).asOption[FormicSystem]
    formicSystemOption match {

      case Some(formicSystem) =>
        val string = new FormicString(() => {}, formicSystem)
        val end = TimeHelper.nowMillis
        val modifiedSession = session.set(SessionVariables.LINEAR_DATA_TYPE, string)
        FormicActions.logTimingValues(start, end, session, statsEngine, name)
        next ! modifiedSession

      case None => throw new IllegalArgumentException("Users have to connect first!")
    }
  }


}
