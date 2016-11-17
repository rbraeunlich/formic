package de.tu_berlin.formic.common.message

import de.tu_berlin.formic.common.datatype.{DataTypeName, DataTypeOperation}
import de.tu_berlin.formic.common.json.FormicJsonDataTypeProtocol
import de.tu_berlin.formic.common.{ClientId, DataTypeInstanceId, OperationId}
import upickle.Js

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

/**
  * This messages is sent as an answer to an UpdateRequest and also when a new data type instance is being created
  * @param dataTypeInstanceId the id of the data type instance
  * @param dataType the data type name
  * @param data the actual data as JSON
  */
case class UpdateResponse(dataTypeInstanceId: DataTypeInstanceId, dataType: DataTypeName, data: String) extends FormicMessage

/**
  * A message with which a client indicates that it wants to receive updates from now on from a certain data type instance.
  * @param clientId the client that wants to receive updates
  * @param dataTypeInstanceId the data type instance it wants to receive updates from
  */
case class UpdateRequest(clientId: ClientId, dataTypeInstanceId: DataTypeInstanceId) extends FormicMessage

case class OperationMessage(clientId: ClientId, dataTypeInstanceId: DataTypeInstanceId, dataType: DataTypeName, operations: List[DataTypeOperation]) extends FormicMessage