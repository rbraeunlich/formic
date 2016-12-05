package de.tu_berlin.formic.datatype.linear.client

import akka.actor.ActorRef
import de.tu_berlin.formic.common.DataTypeInstanceId
import de.tu_berlin.formic.common.datatype.DataTypeName
import de.tu_berlin.formic.common.datatype.client.DataTypeInitiator
import upickle.default._

/**
  * @author Ronny Bräunlich
  */
class FormicIntegerListDataTypeFactory(initiator: DataTypeInitiator) extends FormicLinearDataTypeFactory[Int](initiator) {

  override def createWrapperType(dataTypeInstanceId: DataTypeInstanceId, dataType: ActorRef): FormicList[Int] = {
    new FormicIntegerList(() => {}, initiator, dataTypeInstanceId, dataType)
  }

  override val name: DataTypeName = FormicIntegerListDataTypeFactory.dataTypeName
}

object FormicIntegerListDataTypeFactory {
  val dataTypeName = DataTypeName("IntegerList")
}