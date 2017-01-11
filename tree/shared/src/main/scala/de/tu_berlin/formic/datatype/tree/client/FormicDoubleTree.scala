package de.tu_berlin.formic.datatype.tree.client

import akka.actor.ActorRef
import de.tu_berlin.formic.common.DataTypeInstanceId
import de.tu_berlin.formic.common.datatype.client.DataTypeInitiator

/**
  * @author Ronny Bräunlich
  */
class FormicDoubleTree(callback: () => Unit,
                       initiator: DataTypeInitiator,
                       dataTypeInstanceId: DataTypeInstanceId = DataTypeInstanceId())
  extends FormicTree[Double](callback, initiator, dataTypeInstanceId, FormicDoubleTreeFactory.name) {

  def this(callback: () => Unit, initiator: DataTypeInitiator, dataTypeInstanceId: DataTypeInstanceId, wrapped: ActorRef) {
    this(callback, initiator, dataTypeInstanceId)
    this.actor = wrapped
  }

}