package de.tu_berlin.formic.gatling.action.linear

import de.tu_berlin.formic.datastructure.linear.client.FormicString
import de.tu_berlin.formic.gatling.action.{SessionVariables, TimeMeasureCallback}
import io.gatling.commons.util.TimeHelper
import io.gatling.core.action.{Action, ChainableAction}
import io.gatling.core.session.{Session, _}
import io.gatling.core.stats.StatsEngine

/**
  * @author Ronny Bräunlich
  */
case class LinearDeletion(dataTypeInstanceId: Expression[String], index: Expression[Int], statsEngine: StatsEngine, next: Action) extends ChainableAction {

  override def name: String = "LinearDelete action"

  override def execute(session: Session): Unit = {
    val start = TimeHelper.nowMillis
    dataTypeInstanceId.apply(session).foreach { id =>
      val dataTypeAttribute = session(id)
      val validatedIndex = index.apply(session)
      validatedIndex.foreach(i =>
        dataTypeAttribute.asOption[FormicString] match {
          case None => throw new IllegalArgumentException("Data type not found. Create it first!")
          case Some(dataType) =>
            val opId = dataType.remove(i)
            session(SessionVariables.TIMEMEASURE_CALLBACK).as[TimeMeasureCallback]
              .addListener(TimeMeasureCallback.RemoteOperationTimeMeasureListener(opId, start, session, statsEngine, name))
        })
      next ! session
    }
  }
}
