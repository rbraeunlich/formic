package de.tu_berlin.formic.common.datatype

import akka.actor.ActorRef
import de.tu_berlin.formic.common.{ClientId, DataTypeInstanceId}
import de.tu_berlin.formic.common.datatype.client.AbstractClientDataType.ReceiveCallback
import de.tu_berlin.formic.common.datatype.client.DataTypeInitiator
import de.tu_berlin.formic.common.message.OperationMessage

/**
  * This interface hides the Actor implementation of a data type behind a regular interface
  *
  * @author Ronny Bräunlich
  */
abstract class FormicDataType(private var _callback: () => Unit,
                              val dataTypeName: DataTypeName,
                              var actor: ActorRef = null,
                              var clientId: ClientId = null,
                              val dataTypeInstanceId: DataTypeInstanceId,
                              initiator: DataTypeInitiator) {

  def callback = _callback

  def callback_=(newCallback: () => Unit) {
    _callback = newCallback
    actor ! ReceiveCallback(newCallback)
  }

  initiator.initDataType(this)

}

object FormicDataType {

  case class LocalOperationMessage(op: OperationMessage)

}
