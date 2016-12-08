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
  * A response from the server, acknowledging to the client that the data type has been instantiated.
  *
  */
case class CreateResponse(dataTypeInstanceId: DataTypeInstanceId) extends FormicMessage

/**
  * A request from the client to create a new data type instance
  * @param clientId the client that created the data type instance
  * @param dataTypeInstanceId the id the client gave the data type
  * @param dataType the name of the data type, needed to find the proper factory
  */
case class CreateRequest(clientId: ClientId, dataTypeInstanceId: DataTypeInstanceId, dataType: DataTypeName) extends FormicMessage

/**
  * If a client was disconnected and missed some operations it can send this request.
  * @param clientId The client that is missing operations
  * @param dataTypeInstanceId The data type the client needs the operations for
  * @param sinceId the operation id of the last operation the client knows about
  */
case class HistoricOperationRequest(clientId: ClientId, dataTypeInstanceId: DataTypeInstanceId, sinceId: OperationId) extends FormicMessage

/**
  * This messages is sent as an answer to an UpdateRequest and also when a new data type instance is being created
  * @param dataTypeInstanceId the id of the data type instance
  * @param dataType the data type name
  * @param data the actual data as JSON
  * @param lastOperationId the id of the last operation applied, when this response was created, might be empty
  */
case class UpdateResponse(dataTypeInstanceId: DataTypeInstanceId, dataType: DataTypeName, data: String, lastOperationId: Option[OperationId]) extends FormicMessage

/**
  * A message with which a client indicates that it wants to receive updates from now on from a certain data type instance.
  * @param clientId the client that wants to receive updates
  * @param dataTypeInstanceId the data type instance it wants to receive updates from
  */
case class UpdateRequest(clientId: ClientId, dataTypeInstanceId: DataTypeInstanceId) extends FormicMessage

/**
  * A message containing operations. If a client made a normal change to a data type instance this message contains
  * a single operations. After being applied the same message is distributed to all clients. This message can
  * also be an answer to a HistoricOperationRequest. In that case it can contain zero to n operations.
  * @param clientId The client that applied the operation or that want to receive operations
  * @param dataTypeInstanceId the data type that changed
  * @param dataType the type of the data type that changed, needed for JSON deserialization
  * @param operations the operation/s that shall be applied
  */
case class OperationMessage(clientId: ClientId, dataTypeInstanceId: DataTypeInstanceId, dataType: DataTypeName, operations: List[DataTypeOperation]) extends FormicMessage