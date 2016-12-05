package de.tu_berlin.formic.datatype.linear.client

import akka.actor.ActorRef
import de.tu_berlin.formic.client.datatype.{AbstractClientDataTypeFactory, DataTypeInitiator}
import de.tu_berlin.formic.common.DataTypeInstanceId
import de.tu_berlin.formic.common.controlalgo.GoogleWaveOTClient
import de.tu_berlin.formic.common.datatype.DataTypeName
import de.tu_berlin.formic.common.message.OperationMessage
import de.tu_berlin.formic.datatype.linear.LinearServerDataType

/**
  * @author Ronny Bräunlich
  */
abstract class FormicListFactory(initiator: DataTypeInitiator) extends AbstractClientDataTypeFactory[LinearServerDataType[String], FormicList[_]](initiator){


  override val name: DataTypeName = FormicListFactory.name
}

object FormicListFactory {
  val name: DataTypeName = DataTypeName("linear")
}