package de.tu_berlin.formic.datatype.linear.client

import akka.actor.ActorRef
import de.tu_berlin.formic.common.{ClientId, DataTypeInstanceId}
import de.tu_berlin.formic.common.datatype.DataTypeName
import upickle.default._

/**
  * @author Ronny Bräunlich
  */
class FormicStringDataTypeFactory extends FormicLinearDataTypeFactory[Char] {

  override def createWrapperType(dataTypeInstanceId: DataTypeInstanceId, dataType: ActorRef, localClientId: ClientId): FormicList[Char] = {
    new FormicString(() => {}, RemoteDataTypeInitiator, dataTypeInstanceId, dataType, localClientId)
  }

  override val name: DataTypeName = FormicStringDataTypeFactory.dataTypeName
}

object FormicStringDataTypeFactory {
  val dataTypeName = DataTypeName("string")
}