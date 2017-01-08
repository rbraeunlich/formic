package de.tu_berlin.formic.datatype.tree.client

import akka.actor.ActorRef
import de.tu_berlin.formic.common.controlalgo.WaveOTClient
import de.tu_berlin.formic.common.datatype.client.AbstractClientDataTypeFactory
import de.tu_berlin.formic.common.message.OperationMessage
import de.tu_berlin.formic.common.{DataTypeInstanceId, OperationId}
import upickle.default._
/**
  * @author Ronny Bräunlich
  */
abstract class FormicTreeDataTypeFactory[S](implicit val writer: Writer[S], val reader: Reader[S]) extends AbstractClientDataTypeFactory[TreeClientDataType[S], FormicTree[S]] {

  override def createDataType(dataTypeInstanceId: DataTypeInstanceId, outgoingConnection: ActorRef, data: Option[String], lastOperationId: Option[OperationId]): TreeClientDataType[S] = {
    TreeClientDataType(
      dataTypeInstanceId,
      new WaveOTClient((op) => outgoingConnection ! OperationMessage(null, dataTypeInstanceId, name, List(op))),
      name,
      data,
      lastOperationId,
      outgoingConnection
    )
  }
}