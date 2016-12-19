package de.tu_berlin.formic.datatype.tree

import akka.actor.ActorRef
import de.tu_berlin.formic.common.DataTypeInstanceId
import de.tu_berlin.formic.common.datatype.client.DataTypeInitiator

/**
  * @author Ronny Bräunlich
  */
class FormicIntegerTree(callback: () => Unit,
                        initiator: DataTypeInitiator,
                        dataTypeInstanceId: DataTypeInstanceId)
  extends FormicTree[Int](callback, initiator, dataTypeInstanceId, FormicIntegerTreeFactory.name) {

  def this(callback: () => Unit, initiator: DataTypeInitiator, dataTypeInstanceId: DataTypeInstanceId, wrapped: ActorRef){
    this(callback, initiator, dataTypeInstanceId)
    this.actor = wrapped
  }

}
