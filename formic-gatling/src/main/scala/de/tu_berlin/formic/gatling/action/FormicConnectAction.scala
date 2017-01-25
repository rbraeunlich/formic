package de.tu_berlin.formic.gatling.action

import com.typesafe.config.Config
import de.tu_berlin.formic.client.FormicSystemFactory
import de.tu_berlin.formic.datatype.json.client.JsonClientDataTypeProvider
import de.tu_berlin.formic.datatype.linear.client.LinearClientDataTypeProvider
import de.tu_berlin.formic.datatype.tree.client.TreeClientDataTypeProvider
import io.gatling.commons.util.TimeHelper
import io.gatling.core.action.{Action, ChainableAction}
import io.gatling.core.session.Session
import io.gatling.core.stats.StatsEngine

/**
  * @author Ronny Bräunlich
  */
case class FormicConnectAction(config: Config, statsEngine: StatsEngine, next: Action) extends ChainableAction {

  override def name: String = "Connect action"

  override def execute(session: Session): Unit = {
    val start = TimeHelper.nowMillis
    val formicSystem = FormicSystemFactory.create(config, Set(LinearClientDataTypeProvider(), TreeClientDataTypeProvider(), JsonClientDataTypeProvider()))
    val timeMeasureCallback = new TimeMeasureCallback
    val callback = new CollectingCallbackWithListener(timeMeasureCallback)
    formicSystem.init(callback)
    val end = TimeHelper.nowMillis
    //we use this action as a little initialization
    val modifiedSession = session
      .set(SessionVariables.FORMIC_SYSTEM, formicSystem)
      .set(SessionVariables.CALLBACK, callback)
      .set(SessionVariables.TIMEMEASURE_CALLBACK, timeMeasureCallback)
    FormicActions.logOkTimingValues(start, end, session, statsEngine, name)
    next ! modifiedSession
  }
}

