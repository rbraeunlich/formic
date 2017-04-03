package de.tu_berlin.formic.gatling.action.tree

import de.tu_berlin.formic.client.FormicSystem
import de.tu_berlin.formic.common.DataStructureInstanceId
import de.tu_berlin.formic.datastructure.tree.client.FormicIntegerTree
import de.tu_berlin.formic.gatling.action.TimeMeasureCallback.CreateResponseTimeMeasureListener
import de.tu_berlin.formic.gatling.action.{FormicActions, SessionVariables, TimeMeasureCallback}
import io.gatling.commons.util.TimeHelper
import io.gatling.core.action.{Action, ChainableAction}
import io.gatling.core.session.{Expression, Session}
import io.gatling.core.stats.StatsEngine

/**
  * @author Ronny Bräunlich
  */
case class TreeCreation(dataTypeInstanceId: Expression[String], statsEngine: StatsEngine, next: Action) extends ChainableAction {

  override def name: String = "CreateTreeDataType action"

  override def execute(session: Session): Unit = {
    val start = TimeHelper.nowMillis
    val formicSystemOption = session(SessionVariables.FORMIC_SYSTEM).asOption[FormicSystem]
    val validatedDataTypeInstanceId = dataTypeInstanceId.apply(session)
    validatedDataTypeInstanceId.foreach { id =>
      formicSystemOption match {

        case Some(formicSystem) =>
          val callback = session(SessionVariables.TIMEMEASURE_CALLBACK).as[TimeMeasureCallback]
          val tree = new FormicIntegerTree(callback.callbackMethod, formicSystem, DataStructureInstanceId.valueOf(id))
          val modifiedSession = session.set(id, tree)
          callback.addListener(CreateResponseTimeMeasureListener(tree.dataStructureInstanceId, start, session, statsEngine, name))
          next ! modifiedSession

        case None => throw new IllegalArgumentException("Users have to connect first!")
      }
    }
  }
}
