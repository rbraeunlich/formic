package de.tu_berlin.formic.datastructure.tree.client

import akka.actor.ActorRef
import de.tu_berlin.formic.common.datastructure.client.{ClientDataStructureEvent, DataStructureInitiator}
import de.tu_berlin.formic.common.{ClientId, DataStructureInstanceId}

/**
  * @author Ronny Bräunlich
  */
class FormicIntegerTree(callback: (ClientDataStructureEvent) => Unit,
                        initiator: DataStructureInitiator,
                        dataTypeInstanceId: DataStructureInstanceId = DataStructureInstanceId())
  extends FormicTree[Int](callback, initiator, dataTypeInstanceId, FormicIntegerTreeFactory.name) {

  def this(callback: (ClientDataStructureEvent) => Unit, initiator: DataStructureInitiator, dataTypeInstanceId: DataStructureInstanceId, wrapped: ActorRef, localClientId: ClientId) {
    this(callback, initiator, dataTypeInstanceId)
    this.actor = wrapped
    this.clientId = localClientId
  }

}
