package de.tu_berlin.formic.common.datastructure

import akka.actor.ActorRef
import de.tu_berlin.formic.common.{ClientId, DataStructureInstanceId}
import de.tu_berlin.formic.common.datastructure.client.AbstractClientDataStructure.ReceiveCallback
import de.tu_berlin.formic.common.datastructure.client.{ClientDataStructureEvent, DataStructureInitiator}
import de.tu_berlin.formic.common.message.OperationMessage

/**
  * This interface hides the Actor implementation of a data structure behind a regular interface
  *
  * @author Ronny Bräunlich
  */
abstract class FormicDataStructure(private var _callback: (ClientDataStructureEvent) => Unit,
                                   val dataStructureName: DataStructureName,
                                   var actor: ActorRef = null,
                                   var clientId: ClientId = null,
                                   val dataStructureInstanceId: DataStructureInstanceId,
                                   initiator: DataStructureInitiator) {

  def callback = _callback

  def callback_=(newCallback: (ClientDataStructureEvent) => Unit) {
    _callback = newCallback
    actor ! ReceiveCallback(newCallback)
  }

  initiator.initDataStructure(this)

}

object FormicDataStructure {

  case class LocalOperationMessage(op: OperationMessage)

}
