package de.tu_berlin.formic.common.datatype.client

import de.tu_berlin.formic.common.DataStructureInstanceId
import de.tu_berlin.formic.common.datatype.DataStructureOperation

/**
  * Events the AbstractClientDataType uses to pass information to its callback interface.
  * @author Ronny Bräunlich
  */
sealed trait ClientDataTypeEvent

case class LocalOperationEvent(operation: DataStructureOperation) extends  ClientDataTypeEvent

case class RemoteOperationEvent(operation: DataStructureOperation) extends ClientDataTypeEvent

case class CreateResponseEvent(dataTypeInstanceId: DataStructureInstanceId) extends ClientDataTypeEvent

case class AcknowledgementEvent(operation: DataStructureOperation) extends ClientDataTypeEvent
