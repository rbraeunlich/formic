package de.tu_berlin.formic.common.message

import de.tu_berlin.formic.common.{ClientId, DataTypeInstanceId, OperationId}
import de.tu_berlin.formic.datatype.common.{DataTypeName, DataTypeOperation}

/**
  * @author Ronny Bräunlich
  */
sealed trait FormicMessage

/**
  * A response from the server, indicating that a data type instance has been created.
  *
  * @author Ronny Bräunlich
  */
case class CreateResponse(dataTypeInstanceId: DataTypeInstanceId) extends FormicMessage

case class CreateRequest(clientId: ClientId, dataTypeInstanceId: DataTypeInstanceId, dataType: DataTypeName) extends FormicMessage

case class HistoricOperationRequest(clientId: ClientId, dataTypeInstanceId: DataTypeInstanceId, sinceId: OperationId) extends FormicMessage

case class UpdateResponse(dataTypeInstanceId: DataTypeInstanceId, dataType: DataTypeName, data: String) extends FormicMessage

case class UpdateRequest(clientId: ClientId, dataTypeInstanceId: DataTypeInstanceId) extends FormicMessage

case class OperationMessage(clientId: ClientId, dataTypeInstanceId: DataTypeInstanceId, dataType: DataTypeName, operations: List[DataTypeOperation]) extends FormicMessage
