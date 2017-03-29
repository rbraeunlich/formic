package de.tu_berlin.formic.common.datatype

import akka.actor.ActorRef
import de.tu_berlin.formic.common.{ClientId, DataStructureInstanceId}
import de.tu_berlin.formic.common.datatype.client.AbstractClientDataStructure.ReceiveCallback
import de.tu_berlin.formic.common.datatype.client.{ClientDataTypeEvent, DataStructureInitiator}
import de.tu_berlin.formic.common.message.OperationMessage

/**
  * This interface hides the Actor implementation of a data structure behind a regular interface
  *
  * @author Ronny Bräunlich
  */
abstract class FormicDataStructure(private var _callback: (ClientDataTypeEvent) => Unit,
                                   val dataStructureName: DataStructureName,
                                   var actor: ActorRef = null,
                                   var clientId: ClientId = null,
                                   val dataStructureInstanceId: DataStructureInstanceId,
                                   initiator: DataStructureInitiator) {

  def callback = _callback

  def callback_=(newCallback: (ClientDataTypeEvent) => Unit) {
    _callback = newCallback
    actor ! ReceiveCallback(newCallback)
  }

  initiator.initDataStructure(this)

}

object FormicDataStructure {

  case class LocalOperationMessage(op: OperationMessage)

}
