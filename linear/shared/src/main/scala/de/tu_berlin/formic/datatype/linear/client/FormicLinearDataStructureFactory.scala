package de.tu_berlin.formic.datatype.linear.client

import akka.actor.ActorRef
import de.tu_berlin.formic.common.controlalgo.WaveOTClient
import de.tu_berlin.formic.common.datatype.client.AbstractClientDataStructureFactory
import de.tu_berlin.formic.common.message.OperationMessage
import de.tu_berlin.formic.common.{DataStructureInstanceId, OperationId}
import upickle.default._

/**
  * @author Ronny Bräunlich
  */
abstract class FormicLinearDataStructureFactory[S](implicit val writer: Writer[S], val reader: Reader[S]) extends AbstractClientDataStructureFactory[LinearClientDataStructure[S], FormicList[S]]{

  override def createDataType(dataTypeInstanceId: DataStructureInstanceId, outgoingConnection: ActorRef, data: Option[String], lastOperationId: Option[OperationId]): LinearClientDataStructure[S] = {
    LinearClientDataStructure(
      dataTypeInstanceId,
      new WaveOTClient((op) => outgoingConnection ! OperationMessage(null, dataTypeInstanceId, name, List(op))),
      name,
      data,
      lastOperationId,
      outgoingConnection
    )
  }
}
