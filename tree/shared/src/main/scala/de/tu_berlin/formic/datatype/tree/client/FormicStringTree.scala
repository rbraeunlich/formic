package de.tu_berlin.formic.datatype.tree.client

import akka.actor.ActorRef
import de.tu_berlin.formic.common.{ClientId, DataTypeInstanceId}
import de.tu_berlin.formic.common.datatype.client.DataTypeInitiator

/**
  * @author Ronny Bräunlich
  */
class FormicStringTree(callback: () => Unit,
                       initiator: DataTypeInitiator,
                       dataTypeInstanceId: DataTypeInstanceId = DataTypeInstanceId())
  extends FormicTree[String](callback, initiator, dataTypeInstanceId, FormicStringTreeFactory.name) {

  def this(callback: () => Unit, initiator: DataTypeInitiator, dataTypeInstanceId: DataTypeInstanceId, wrapped: ActorRef, localClientId: ClientId) {
    this(callback, initiator, dataTypeInstanceId)
    this.actor = wrapped
    this.clientId = localClientId
  }

}