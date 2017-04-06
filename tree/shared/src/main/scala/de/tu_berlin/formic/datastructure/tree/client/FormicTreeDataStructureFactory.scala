package de.tu_berlin.formic.datastructure.tree.client

import akka.actor.ActorRef
import de.tu_berlin.formic.common.controlalgo.WaveOTClient
import de.tu_berlin.formic.common.datastructure.client.AbstractClientDataStructureFactory
import de.tu_berlin.formic.common.message.OperationMessage
import de.tu_berlin.formic.common.{DataStructureInstanceId, OperationId}
import upickle.default._
/**
  * @author Ronny Bräunlich
  */
abstract class FormicTreeDataStructureFactory[S](implicit val writer: Writer[S], val reader: Reader[S]) extends AbstractClientDataStructureFactory[TreeClientDataStructure[S], FormicTree[S]] {

  override def createDataStructure(dataTypeInstanceId: DataStructureInstanceId, outgoingConnection: ActorRef, data: Option[String], lastOperationId: Option[OperationId]): TreeClientDataStructure[S] = {
    TreeClientDataStructure(
      dataTypeInstanceId,
      new WaveOTClient((op) => outgoingConnection ! OperationMessage(null, dataTypeInstanceId, name, List(op))),
      name,
      data,
      lastOperationId,
      outgoingConnection
    )
  }
}