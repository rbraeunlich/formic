package de.tu_berlin.formic.datatype.linear.client

import akka.actor.ActorRef
import de.tu_berlin.formic.common.{ClientId, DataTypeInstanceId}
import de.tu_berlin.formic.common.datatype.client.DataTypeInitiator
import upickle.default._

import scala.scalajs.js.annotation.JSExport
/**
  * @author Ronny Bräunlich
  */
@JSExport
class FormicDoubleList(callback: () => Unit, initiator: DataTypeInitiator, dataTypeInstanceId: DataTypeInstanceId = DataTypeInstanceId())
  extends FormicList[Double](callback, initiator, dataTypeInstanceId, FormicDoubleListDataTypeFactory.name) {

  def this(callback: () => Unit, initiator: DataTypeInitiator, dataTypeInstanceId: DataTypeInstanceId, wrapped: ActorRef, localClientId: ClientId) {
    this(callback, initiator, dataTypeInstanceId)
    this.actor = wrapped
    this.clientId = localClientId
  }
}
