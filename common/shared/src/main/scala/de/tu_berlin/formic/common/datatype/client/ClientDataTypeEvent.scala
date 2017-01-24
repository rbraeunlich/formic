package de.tu_berlin.formic.common.datatype.client

import de.tu_berlin.formic.common.datatype.DataTypeOperation

/**
  * Events the AbstractClientDataType uses to pass information to its callback interface.
  * @author Ronny Bräunlich
  */
sealed trait ClientDataTypeEvent

case class LocalOperationEvent(operation: DataTypeOperation) extends  ClientDataTypeEvent

case class RemoteOperationEvent(operation: DataTypeOperation) extends ClientDataTypeEvent

case object CreateResponseEvent extends ClientDataTypeEvent