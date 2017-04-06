package de.tu_berlin.formic.datastructure.json.client

import akka.actor.ActorRef
import de.tu_berlin.formic.common.controlalgo.WaveOTClient
import de.tu_berlin.formic.common.datastructure.DataStructureName
import de.tu_berlin.formic.common.datastructure.client.AbstractClientDataStructureFactory
import de.tu_berlin.formic.common.message.OperationMessage
import de.tu_berlin.formic.common.{ClientId, DataStructureInstanceId, OperationId}
import de.tu_berlin.formic.datastructure.tree.client.RemoteDataStructureInitiator

/**
  * @author Ronny Bräunlich
  */
class FormicJsonObjectFactory extends AbstractClientDataStructureFactory[JsonClientDataStructure, FormicJsonObject]{

  override def createDataStructure(dataStructureInstanceId: DataStructureInstanceId, outgoingConnection: ActorRef, data: Option[String], lastOperationId: Option[OperationId]): JsonClientDataStructure = {
    JsonClientDataStructure(
      dataStructureInstanceId,
      new WaveOTClient((op) => outgoingConnection ! OperationMessage(null, dataStructureInstanceId, name, List(op))),
      name,
      data,
      lastOperationId,
      outgoingConnection
    )
  }

  override def createWrapper(dataStructureInstanceId: DataStructureInstanceId, dataStructure: ActorRef, localClientId: ClientId): FormicJsonObject = {
    new FormicJsonObject((ClientDataTypeEvent) => {}, RemoteDataStructureInitiator, dataStructureInstanceId, dataStructure, localClientId)
  }

  override val name: DataStructureName = FormicJsonObjectFactory.name
}

object FormicJsonObjectFactory {
  val name = DataStructureName("json")
}
