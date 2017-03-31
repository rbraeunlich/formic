package de.tu_berlin.formic.datatype.json.client

import akka.actor.ActorRef
import de.tu_berlin.formic.common.controlalgo.WaveOTClient
import de.tu_berlin.formic.common.datatype.DataStructureName
import de.tu_berlin.formic.common.datatype.client.{AbstractClientDataStructureFactory, ClientDataStructureEvent}
import de.tu_berlin.formic.common.message.OperationMessage
import de.tu_berlin.formic.common.{ClientId, DataStructureInstanceId, OperationId}
import de.tu_berlin.formic.datatype.tree.client.RemoteDataStructureInitiator

/**
  * @author Ronny Bräunlich
  */
class FormicJsonObjectFactory extends AbstractClientDataStructureFactory[JsonClientDataStructure, FormicJsonObject]{

  override def createDataType(dataTypeInstanceId: DataStructureInstanceId, outgoingConnection: ActorRef, data: Option[String], lastOperationId: Option[OperationId]): JsonClientDataStructure = {
    JsonClientDataStructure(
      dataTypeInstanceId,
      new WaveOTClient((op) => outgoingConnection ! OperationMessage(null, dataTypeInstanceId, name, List(op))),
      name,
      data,
      lastOperationId,
      outgoingConnection
    )
  }

  override def createWrapperType(dataTypeInstanceId: DataStructureInstanceId, dataType: ActorRef, localClientId: ClientId): FormicJsonObject = {
    new FormicJsonObject((ClientDataTypeEvent) => {}, RemoteDataStructureInitiator, dataTypeInstanceId, dataType, localClientId)
  }

  override val name: DataStructureName = FormicJsonObjectFactory.name
}

object FormicJsonObjectFactory {
  val name = DataStructureName("json")
}
