package de.tu_berlin.formic.datatype.tree.client

import akka.actor.ActorRef
import de.tu_berlin.formic.common.{ClientId, DataStructureInstanceId}
import de.tu_berlin.formic.common.datatype.client.{ClientDataTypeEvent, DataStructureInitiator}

/**
  * @author Ronny Bräunlich
  */
class FormicDoubleTree(callback: (ClientDataTypeEvent) => Unit,
                       initiator: DataStructureInitiator,
                       dataTypeInstanceId: DataStructureInstanceId = DataStructureInstanceId())
  extends FormicTree[Double](callback, initiator, dataTypeInstanceId, FormicDoubleTreeFactory.name) {

  def this(callback: (ClientDataTypeEvent) => Unit, initiator: DataStructureInitiator, dataTypeInstanceId: DataStructureInstanceId, wrapped: ActorRef, localClientId: ClientId) {
    this(callback, initiator, dataTypeInstanceId)
    this.actor = wrapped
    this.clientId = localClientId
  }

}