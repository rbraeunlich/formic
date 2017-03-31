package de.tu_berlin.formic.common.message

import de.tu_berlin.formic.common.datastructure.{DataStructureName, DataStructureOperation}
import de.tu_berlin.formic.common.{ClientId, DataStructureInstanceId, OperationId}

/**
  * @author Ronny Bräunlich
  */
sealed trait FormicMessage

/**
  * A response from the server, acknowledging to the client that the data structure has been instantiated.
  *
  */
case class CreateResponse(dataStructureInstanceId: DataStructureInstanceId) extends FormicMessage

/**
  * A request from the client to create a new data structure instance
  * @param clientId the client that created the data structure instance
  * @param dataStructureInstanceId the id the client gave the data structure
  * @param dataStructure the name of the data structure, needed to find the proper factory
  */
case class CreateRequest(clientId: ClientId, dataStructureInstanceId: DataStructureInstanceId, dataStructure: DataStructureName) extends FormicMessage

/**
  * If a client was disconnected and missed some operations it can send this request.
  * @param clientId The client that is missing operations
  * @param dataStructureInstanceId The data structure the client needs the operations for
  * @param sinceId the operation id of the last operation the client knows about, might be null
  */
case class HistoricOperationRequest(clientId: ClientId, dataStructureInstanceId: DataStructureInstanceId, sinceId: OperationId) extends FormicMessage

/**
  * This messages is sent as an answer to an UpdateRequest and also when a new data structure instance is being created
  * @param dataStructureInstanceId the id of the data structure instance
  * @param dataStructure the data structure name
  * @param data the actual data as JSON
  * @param lastOperationId the id of the last operation applied, when this response was created, might be empty
  */
case class UpdateResponse(dataStructureInstanceId: DataStructureInstanceId, dataStructure: DataStructureName, data: String, lastOperationId: Option[OperationId]) extends FormicMessage

/**
  * A message with which a client indicates that it wants to receive updates from now on from a certain data structure instance.
  * @param clientId the client that wants to receive updates
  * @param dataStructureInstanceId the data structure instance it wants to receive updates from
  */
case class UpdateRequest(clientId: ClientId, dataStructureInstanceId: DataStructureInstanceId) extends FormicMessage

/**
  * A message containing operations. If a client made a normal change to a data structure instance this message contains
  * a single operations. After being applied the same message is distributed to all clients. This message can
  * also be an answer to a HistoricOperationRequest. In that case it can contain zero to n operations.
  * @param clientId The client that applied the operation or that want to receive operations
  * @param dataStructureInstanceId the data stru that changed
  * @param dataStructure the structure of the data structure that changed, needed for JSON deserialization
  * @param operations the operation/s that shall be applied
  */
case class OperationMessage(clientId: ClientId, dataStructureInstanceId: DataStructureInstanceId, dataStructure: DataStructureName, operations: List[DataStructureOperation]) extends FormicMessage