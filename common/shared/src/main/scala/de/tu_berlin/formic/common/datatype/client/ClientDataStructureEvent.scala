package de.tu_berlin.formic.common.datatype.client

import de.tu_berlin.formic.common.DataStructureInstanceId
import de.tu_berlin.formic.common.datatype.DataStructureOperation

/**
  * Events the AbstractClientDataStructure uses to pass information to its callback interface.
  * @author Ronny Bräunlich
  */
sealed trait ClientDataStructureEvent

case class LocalOperationEvent(operation: DataStructureOperation) extends  ClientDataStructureEvent

case class RemoteOperationEvent(operation: DataStructureOperation) extends ClientDataStructureEvent

case class CreateResponseEvent(dataTypeInstanceId: DataStructureInstanceId) extends ClientDataStructureEvent

case class AcknowledgementEvent(operation: DataStructureOperation) extends ClientDataStructureEvent
