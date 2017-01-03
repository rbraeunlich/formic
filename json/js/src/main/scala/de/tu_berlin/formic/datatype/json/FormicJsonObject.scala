package de.tu_berlin.formic.datatype.json

import akka.actor.ActorRef
import de.tu_berlin.formic.common.DataTypeInstanceId
import de.tu_berlin.formic.common.datatype.FormicDataType
import de.tu_berlin.formic.common.datatype.client.DataTypeInitiator

/**
  * @author Ronny Bräunlich
  */
class FormicJsonObject(callback: () => Unit,
                       initiator: DataTypeInitiator,
                       dataTypeInstanceId: DataTypeInstanceId) extends FormicDataType(callback, FormicJsonObjectFactory.name, dataTypeInstanceId = dataTypeInstanceId, initiator = initiator) {

  def this(callback: () => Unit, initiator: DataTypeInitiator, dataTypeInstanceId: DataTypeInstanceId, wrapped: ActorRef) {
    this(callback, initiator, dataTypeInstanceId)
    this.actor = wrapped
  }
}
