package de.tu_berlin.formic.common.datatype

import akka.actor.ActorRef
import de.tu_berlin.formic.common.DataTypeInstanceId
import de.tu_berlin.formic.common.message.OperationMessage

/**
  * This interface hides the Actor implementation of a data type behind a regular interface
  *
  * @author Ronny Bräunlich
  */
trait FormicDataType {

  var connection: ActorRef = _

  val dataTypeName: DataTypeName

  var callback: () => Unit

  var actor: ActorRef = _

  val dataTypeInstanceId: DataTypeInstanceId
}

object FormicDataType {

  case class LocalOperationMessage(op: OperationMessage)

}
