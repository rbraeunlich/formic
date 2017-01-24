package de.tu_berlin.formic.datatype.tree.client

import akka.actor.ActorRef
import de.tu_berlin.formic.common.{ClientId, DataTypeInstanceId}
import de.tu_berlin.formic.common.datatype.client.{ClientDataTypeEvent, DataTypeInitiator}

/**
  * @author Ronny Bräunlich
  */
class FormicIntegerTree(callback: (ClientDataTypeEvent) => Unit,
                        initiator: DataTypeInitiator,
                        dataTypeInstanceId: DataTypeInstanceId = DataTypeInstanceId())
  extends FormicTree[Int](callback, initiator, dataTypeInstanceId, FormicIntegerTreeFactory.name) {

  def this(callback: (ClientDataTypeEvent) => Unit, initiator: DataTypeInitiator, dataTypeInstanceId: DataTypeInstanceId, wrapped: ActorRef, localClientId: ClientId) {
    this(callback, initiator, dataTypeInstanceId)
    this.actor = wrapped
    this.clientId = localClientId
  }

}
