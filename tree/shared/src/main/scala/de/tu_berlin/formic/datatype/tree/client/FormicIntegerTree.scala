package de.tu_berlin.formic.datatype.tree.client

import akka.actor.ActorRef
import de.tu_berlin.formic.common.{ClientId, DataStructureInstanceId}
import de.tu_berlin.formic.common.datatype.client.{ClientDataTypeEvent, DataTypeInitiator}

/**
  * @author Ronny Bräunlich
  */
class FormicIntegerTree(callback: (ClientDataTypeEvent) => Unit,
                        initiator: DataTypeInitiator,
                        dataTypeInstanceId: DataStructureInstanceId = DataStructureInstanceId())
  extends FormicTree[Int](callback, initiator, dataTypeInstanceId, FormicIntegerTreeFactory.name) {

  def this(callback: (ClientDataTypeEvent) => Unit, initiator: DataTypeInitiator, dataTypeInstanceId: DataStructureInstanceId, wrapped: ActorRef, localClientId: ClientId) {
    this(callback, initiator, dataTypeInstanceId)
    this.actor = wrapped
    this.clientId = localClientId
  }

}
