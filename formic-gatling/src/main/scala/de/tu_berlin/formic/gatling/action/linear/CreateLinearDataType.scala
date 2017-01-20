package de.tu_berlin.formic.gatling.action.linear

import de.tu_berlin.formic.client.FormicSystem
import de.tu_berlin.formic.common.DataTypeInstanceId
import de.tu_berlin.formic.datatype.linear.client.FormicString
import de.tu_berlin.formic.gatling.action.FormicActions
import io.gatling.commons.util.TimeHelper
import io.gatling.core.action.{Action, ChainableAction}
import io.gatling.core.session.Session
import io.gatling.core.stats.StatsEngine

/**
  * @author Ronny Bräunlich
  */
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
