package de.tu_berlin.formic.gatling.action

import com.typesafe.config.Config
import de.tu_berlin.formic.client.{FormicSystemFactory, NewInstanceCallback}
import de.tu_berlin.formic.common.ClientId
import de.tu_berlin.formic.common.datatype.{DataTypeName, FormicDataType}
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
    formicSystem.init(MockCallback)
    val end = TimeHelper.nowMillis
    val modifiedSession = session.set("FormicSystem", formicSystem)
    FormicActions.logTimingValues(start, end, session, statsEngine, name)
    next ! modifiedSession
  }
}

object MockCallback extends NewInstanceCallback {
  /**
    * Set a new callback interface at a data type instance that was created remotely.
    */
  override def newCallbackFor(instance: FormicDataType, dataType: DataTypeName): () => Unit = () => {}

  /**
    * Perform any initializations necessary for a new, remote data type.
    */
  override def doNewInstanceCreated(instance: FormicDataType, dataType: DataTypeName): Unit = {}
}