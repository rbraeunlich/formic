package de.tu_berlin.formic.datatype.tree.client

import akka.actor.ActorRef
import de.tu_berlin.formic.common.DataTypeInstanceId
import de.tu_berlin.formic.common.datatype.client.DataTypeInitiator

/**
  * @author Ronny Bräunlich
  */
class FormicStringTree(callback: () => Unit,
                       initiator: DataTypeInitiator,
                       dataTypeInstanceId: DataTypeInstanceId)
  extends FormicTree[String](callback, initiator, dataTypeInstanceId, FormicStringTreeFactory.name) {

  def this(callback: () => Unit, initiator: DataTypeInitiator, dataTypeInstanceId: DataTypeInstanceId, wrapped: ActorRef) {
    this(callback, initiator, dataTypeInstanceId)
    this.actor = wrapped
  }

}