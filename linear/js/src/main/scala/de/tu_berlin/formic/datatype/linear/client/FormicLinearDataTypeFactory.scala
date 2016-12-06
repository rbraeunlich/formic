package de.tu_berlin.formic.datatype.linear.client

import akka.actor.ActorRef
import de.tu_berlin.formic.common.DataTypeInstanceId
import de.tu_berlin.formic.common.controlalgo.GoogleWaveOTClient
import de.tu_berlin.formic.common.datatype.client.{AbstractClientDataTypeFactory, DataTypeInitiator}
import de.tu_berlin.formic.common.message.OperationMessage
import upickle.default._

/**
  * @author Ronny Bräunlich
  */
abstract class FormicLinearDataTypeFactory[S](implicit val writer: Writer[S], val reader: Reader[S]) extends AbstractClientDataTypeFactory[LinearClientDataType[S], FormicList[S]]{

  override def createDataType(dataTypeInstanceId: DataTypeInstanceId, outgoingConnection: ActorRef, data: Option[String]): LinearClientDataType[S] = {
    LinearClientDataType(
      dataTypeInstanceId,
      new GoogleWaveOTClient((op) => outgoingConnection ! OperationMessage(null, dataTypeInstanceId, name, List(op))),
      name,
      data
    )
  }
}
