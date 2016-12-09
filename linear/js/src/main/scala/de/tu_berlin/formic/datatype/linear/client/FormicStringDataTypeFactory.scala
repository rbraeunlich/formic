package de.tu_berlin.formic.datatype.linear.client

import akka.actor.ActorRef
import de.tu_berlin.formic.common.DataTypeInstanceId
import de.tu_berlin.formic.common.datatype.DataTypeName
import de.tu_berlin.formic.common.datatype.client.DataTypeInitiator
import upickle.default._

/**
  * @author Ronny Bräunlich
  */
class FormicStringDataTypeFactory extends FormicLinearDataTypeFactory[Char] {

  override def createWrapperType(dataTypeInstanceId: DataTypeInstanceId, dataType: ActorRef): FormicList[Char] = {
    new FormicString(() => {}, RemoteDataTypeInitiator, dataTypeInstanceId, dataType)
  }

  override val name: DataTypeName = FormicStringDataTypeFactory.dataTypeName
}

object FormicStringDataTypeFactory {
  val dataTypeName = DataTypeName("string")
}