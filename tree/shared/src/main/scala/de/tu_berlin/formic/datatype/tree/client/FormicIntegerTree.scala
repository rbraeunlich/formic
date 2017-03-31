package de.tu_berlin.formic.datatype.tree.client

import akka.actor.ActorRef
import de.tu_berlin.formic.common.{ClientId, DataStructureInstanceId}
import de.tu_berlin.formic.common.datastructure.client.{ClientDataStructureEvent, DataStructureInitiator}

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
