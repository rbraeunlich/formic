package de.tu_berlin.formic.datatype.tree.client

import akka.actor.ActorRef
import de.tu_berlin.formic.common.{ClientId, DataStructureInstanceId}
import de.tu_berlin.formic.common.datatype.client.{ClientDataTypeEvent, DataStructureInitiator}

/**
  * @author Ronny Bräunlich
  */
class FormicStringTree(callback: (ClientDataTypeEvent) => Unit,
                       initiator: DataStructureInitiator,
                       dataTypeInstanceId: DataStructureInstanceId = DataStructureInstanceId())
  extends FormicTree[String](callback, initiator, dataTypeInstanceId, FormicStringTreeFactory.name) {

  def this(callback: (ClientDataTypeEvent) => Unit, initiator: DataStructureInitiator, dataTypeInstanceId: DataStructureInstanceId, wrapped: ActorRef, localClientId: ClientId) {
    this(callback, initiator, dataTypeInstanceId)
    this.actor = wrapped
    this.clientId = localClientId
  }

}