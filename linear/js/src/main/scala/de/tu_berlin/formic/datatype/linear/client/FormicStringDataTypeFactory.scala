package de.tu_berlin.formic.datatype.linear.client

import akka.actor.ActorRef
import de.tu_berlin.formic.common.DataTypeInstanceId
import de.tu_berlin.formic.common.datatype.DataTypeName
import de.tu_berlin.formic.common.datatype.client.DataTypeInitiator
import upickle.default._

/**
  * @author Ronny Bräunlich
  */
class FormicStringDataTypeFactory(initiator: DataTypeInitiator) extends FormicLinearDataTypeFactory[Char](initiator) {

  override def createWrapperType(dataTypeInstanceId: DataTypeInstanceId, dataType: ActorRef): FormicList[Char] = {
    new FormicString(() => {}, initiator, dataTypeInstanceId, dataType)
  }

  override val name: DataTypeName = FormicStringDataTypeFactory.dataTypeName
}

object FormicStringDataTypeFactory {
  val dataTypeName = DataTypeName("string")
}