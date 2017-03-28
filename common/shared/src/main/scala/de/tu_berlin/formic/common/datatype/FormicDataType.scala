package de.tu_berlin.formic.common.datatype

import akka.actor.ActorRef
import de.tu_berlin.formic.common.{ClientId, DataStructureInstanceId$}
import de.tu_berlin.formic.common.datatype.client.AbstractClientDataType.ReceiveCallback
import de.tu_berlin.formic.common.datatype.client.{ClientDataTypeEvent, DataTypeInitiator}
import de.tu_berlin.formic.common.message.OperationMessage

/**
  * This interface hides the Actor implementation of a data type behind a regular interface
  *
  * @author Ronny Bräunlich
  */
abstract class FormicDataType(private var _callback: (ClientDataTypeEvent) => Unit,
                              val dataTypeName: DataStructureName,
                              var actor: ActorRef = null,
                              var clientId: ClientId = null,
                              val dataTypeInstanceId: DataStructureInstanceId,
                              initiator: DataTypeInitiator) {

  def callback = _callback

  def callback_=(newCallback: (ClientDataTypeEvent) => Unit) {
    _callback = newCallback
    actor ! ReceiveCallback(newCallback)
  }

  initiator.initDataType(this)

}

object FormicDataType {

  case class LocalOperationMessage(op: OperationMessage)

}
