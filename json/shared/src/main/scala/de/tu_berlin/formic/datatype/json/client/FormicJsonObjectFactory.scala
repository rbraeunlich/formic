package de.tu_berlin.formic.datatype.json.client

import akka.actor.ActorRef
import de.tu_berlin.formic.common.controlalgo.WaveOTClient
import de.tu_berlin.formic.common.datatype.DataTypeName
import de.tu_berlin.formic.common.datatype.client.AbstractClientDataTypeFactory
import de.tu_berlin.formic.common.message.OperationMessage
import de.tu_berlin.formic.common.{ClientId, DataTypeInstanceId, OperationId}
import de.tu_berlin.formic.datatype.tree.client.RemoteDataTypeInitiator

/**
  * @author Ronny Bräunlich
  */
class FormicJsonObjectFactory extends AbstractClientDataTypeFactory[JsonClientDataType, FormicJsonObject]{

  override def createDataType(dataTypeInstanceId: DataTypeInstanceId, outgoingConnection: ActorRef, data: Option[String], lastOperationId: Option[OperationId]): JsonClientDataType = {
    JsonClientDataType(
      dataTypeInstanceId,
      new WaveOTClient((op) => outgoingConnection ! OperationMessage(null, dataTypeInstanceId, name, List(op))),
      name,
      data,
      lastOperationId,
      outgoingConnection
    )
  }

  override def createWrapperType(dataTypeInstanceId: DataTypeInstanceId, dataType: ActorRef, localClientId: ClientId): FormicJsonObject = {
    new FormicJsonObject(() => {}, RemoteDataTypeInitiator, dataTypeInstanceId, dataType, localClientId)
  }

  override val name: DataTypeName = FormicJsonObjectFactory.name
}

object FormicJsonObjectFactory {
  val name = DataTypeName("json")
}
