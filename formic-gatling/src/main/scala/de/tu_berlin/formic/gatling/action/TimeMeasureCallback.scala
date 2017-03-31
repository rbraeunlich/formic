package de.tu_berlin.formic.gatling.action

import de.tu_berlin.formic.common.{DataStructureInstanceId, OperationId}
import de.tu_berlin.formic.common.datatype.client.{AcknowledgementEvent, ClientDataStructureEvent, CreateResponseEvent}
import de.tu_berlin.formic.gatling.action.TimeMeasureCallback.{RemoteOperationTimeMeasureListener, TimeMeasureListener}
import io.gatling.commons.util.TimeHelper
import io.gatling.core.session.Session
import io.gatling.core.stats.StatsEngine

import scala.collection.JavaConverters._

/**
  * This object is used to measure the time an operation needs from the invocation on the
  * FormicDataType until the server sends the acknowledgement.
  *
  * @author Ronny Bräunlich
  */
class TimeMeasureCallback {

  val listener = new java.util.concurrent.CopyOnWriteArrayList[TimeMeasureListener]()

  def callbackMethod(event: ClientDataStructureEvent): Unit = {
    val end = TimeHelper.nowMillis
    val matched = listener.asScala.filter(listener => listener.isOperation(event))
    matched.foreach(t => t.logOk(end))
    listener.removeAll(matched.asJava)
  }

  def addListener[T](listener: TimeMeasureListener): Unit = {
    this.listener.add(listener)
  }

  def cancelAll() = {
    listener.asScala.foreach(l => l.logKo())
  }
}

object TimeMeasureCallback {

  sealed trait TimeMeasureListener {

    val start: Long
    val session: Session
    val statsEngine: StatsEngine
    val name: String

    def isOperation(e: ClientDataStructureEvent): Boolean

    def logOk(end: Long) = {
      FormicActions.logOkTimingValues(start, end, session, statsEngine, name)
    }

    def logKo() = {
      FormicActions.logKoTimingValues(start, TimeHelper.nowMillis, session, statsEngine, name)
    }
  }

  case class RemoteOperationTimeMeasureListener(operationId: OperationId, start: Long, session: Session, statsEngine: StatsEngine, name: String) extends TimeMeasureListener {

    def isOperation(e: ClientDataStructureEvent): Boolean = {
      e.isInstanceOf[AcknowledgementEvent] && e.asInstanceOf[AcknowledgementEvent].operation.id == operationId
    }

  }

  case class CreateResponseTimeMeasureListener(dataTypeInstanceId: DataStructureInstanceId, start: Long, session: Session, statsEngine: StatsEngine, name: String) extends TimeMeasureListener {

    def isOperation(e: ClientDataStructureEvent): Boolean = {
      e.isInstanceOf[CreateResponseEvent] && e.asInstanceOf[CreateResponseEvent].dataTypeInstanceId == dataTypeInstanceId
    }
  }

}
