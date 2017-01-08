package de.tu_berlin.formic.datatype.tree.client

import akka.actor.ActorRef
import de.tu_berlin.formic.common.DataTypeInstanceId
import de.tu_berlin.formic.common.datatype.client.DataTypeInitiator

/**
  * @author Ronny Bräunlich
  */
class FormicBooleanTree(callback: () => Unit,
                        initiator: DataTypeInitiator,
                        dataTypeInstanceId: DataTypeInstanceId)
  extends FormicTree[Boolean](callback, initiator, dataTypeInstanceId, FormicBooleanTreeFactory.name) {

  def this(callback: () => Unit, initiator: DataTypeInitiator, dataTypeInstanceId: DataTypeInstanceId, wrapped: ActorRef) {
    this(callback, initiator, dataTypeInstanceId)
    this.actor = wrapped
  }

}