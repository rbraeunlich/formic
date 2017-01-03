package de.tu_berlin.formic.datatype.json

import akka.actor.ActorRef
import de.tu_berlin.formic.common.controlalgo.WaveOTClient
import de.tu_berlin.formic.common.{DataTypeInstanceId, OperationId}
import de.tu_berlin.formic.common.datatype.DataTypeName
import de.tu_berlin.formic.common.datatype.client.AbstractClientDataTypeFactory
import de.tu_berlin.formic.common.message.OperationMessage
import de.tu_berlin.formic.datatype.tree.RemoteDataTypeInitiator

/**
  * @author Ronny Bräunlich
  */
class FormicJsonFactory extends AbstractClientDataTypeFactory[JsonClientDataType, FormicJsonObject]{

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

  override def createWrapperType(dataTypeInstanceId: DataTypeInstanceId, dataType: ActorRef): FormicJsonObject = {
    new FormicJsonObject(() => {}, RemoteDataTypeInitiator, dataTypeInstanceId, dataType)
  }

  override val name: DataTypeName = FormicJsonFactory.name
}

object FormicJsonFactory {
  val name = DataTypeName("json")
}
