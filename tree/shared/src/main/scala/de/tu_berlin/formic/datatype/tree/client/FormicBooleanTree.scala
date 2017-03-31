package de.tu_berlin.formic.datatype.tree.client

import akka.actor.ActorRef
import de.tu_berlin.formic.common.{ClientId, DataStructureInstanceId}
import de.tu_berlin.formic.common.datatype.client.{ClientDataStructureEvent, DataStructureInitiator}

/**
  * @author Ronny Bräunlich
  */
class FormicBooleanTree(callback: (ClientDataStructureEvent) => Unit,
                        initiator: DataStructureInitiator,
                        dataTypeInstanceId: DataStructureInstanceId = DataStructureInstanceId())
  extends FormicTree[Boolean](callback, initiator, dataTypeInstanceId, FormicBooleanTreeFactory.name) {

  def this(callback: (ClientDataStructureEvent) => Unit, initiator: DataStructureInitiator, dataTypeInstanceId: DataStructureInstanceId, wrapped: ActorRef, localClientId: ClientId) {
    this(callback, initiator, dataTypeInstanceId)
    this.actor = wrapped
    this.clientId = localClientId
  }

}