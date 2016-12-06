package de.tu_berlin.formic.datatype.linear.client

import akka.actor.ActorRef
import de.tu_berlin.formic.common.DataTypeInstanceId
import de.tu_berlin.formic.common.datatype.DataTypeName
import de.tu_berlin.formic.common.datatype.client.DataTypeInitiator
import upickle.default._

import scala.scalajs.js.annotation.JSExport

/**
  * @author Ronny Bräunlich
  */
@JSExport
class FormicBooleanList(callback: () => Unit, initiator: DataTypeInitiator, dataTypeInstanceId: DataTypeInstanceId = DataTypeInstanceId())
  extends FormicList[Boolean](callback, initiator, dataTypeInstanceId, FormicBooleanListDataTypeFactory.dataTypeName) {

  def this(callback: () => Unit, initiator: DataTypeInitiator, dataTypeInstanceId: DataTypeInstanceId, wrapped: ActorRef){
    this(callback, initiator, dataTypeInstanceId)
    this.actor = wrapped
  }
}
