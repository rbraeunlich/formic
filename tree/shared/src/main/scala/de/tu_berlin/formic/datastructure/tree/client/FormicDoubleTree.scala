package de.tu_berlin.formic.datastructure.tree.client

import akka.actor.ActorRef
import de.tu_berlin.formic.common.datastructure.client.{ClientDataStructureEvent, DataStructureInitiator}
import de.tu_berlin.formic.common.{ClientId, DataStructureInstanceId}

/**
  * @author Ronny Bräunlich
  */
class FormicDoubleTree(callback: (ClientDataStructureEvent) => Unit,
                       initiator: DataStructureInitiator,
                       dataStructureInstanceId: DataStructureInstanceId = DataStructureInstanceId())
  extends FormicTree[Double](callback, initiator, dataStructureInstanceId, FormicDoubleTreeFactory.name) {

  def this(callback: (ClientDataStructureEvent) => Unit, initiator: DataStructureInitiator, dataStructureInstanceId: DataStructureInstanceId, wrapped: ActorRef, localClientId: ClientId) {
    this(callback, initiator, dataStructureInstanceId)
    this.actor = wrapped
    this.clientId = localClientId
  }

}