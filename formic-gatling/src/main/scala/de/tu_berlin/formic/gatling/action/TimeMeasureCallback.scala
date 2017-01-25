package de.tu_berlin.formic.gatling.action

import de.tu_berlin.formic.common.OperationId
import de.tu_berlin.formic.common.datatype.client.{AcknowledgementEvent, ClientDataTypeEvent}
import de.tu_berlin.formic.gatling.action.TimeMeasureCallback.RemoteOperationTimeMeasureListener
import io.gatling.commons.util.TimeHelper
import io.gatling.core.session.Session
import io.gatling.core.stats.StatsEngine

import collection.JavaConverters._

/**
  * This object is used to measure the time an operation needs from the invocation on the
  * FormicDataType until the server sends the acknowledgement.
  *
  * @author Ronny Bräunlich
  */
class TimeMeasureCallback {

  val listener = new java.util.concurrent.CopyOnWriteArrayList[RemoteOperationTimeMeasureListener]()

  def callbackMethod(event: ClientDataTypeEvent): Unit = {
    val end = TimeHelper.nowMillis
    val matched = listener.asScala.filter(listener => listener.isOperation(event))
    matched.foreach(t => t.logOk(end))
    listener.removeAll(matched.asJava)
  }

  def addListener[T](listener: RemoteOperationTimeMeasureListener): Unit = {
    this.listener.add(listener)
  }

  def cancelAll() = {
    listener.asScala.foreach(l => l.logKo())
  }
}

object TimeMeasureCallback {

  case class RemoteOperationTimeMeasureListener(operationId: OperationId, start: Long, session: Session, statsEngine: StatsEngine, name: String) {

    def isOperation(e: ClientDataTypeEvent): Boolean = {
      e.isInstanceOf[AcknowledgementEvent] && e.asInstanceOf[AcknowledgementEvent].operation.id == operationId
    }

    def logOk(end: Long) = {
      FormicActions.logOkTimingValues(start, end, session, statsEngine, name)
    }

    def logKo() = {
      FormicActions.logKoTimingValues(start, TimeHelper.nowMillis, session, statsEngine, name)
    }

  }

}
