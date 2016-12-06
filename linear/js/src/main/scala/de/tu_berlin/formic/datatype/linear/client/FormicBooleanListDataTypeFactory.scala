package de.tu_berlin.formic.datatype.linear.client

import akka.actor.ActorRef
import de.tu_berlin.formic.common.DataTypeInstanceId
import de.tu_berlin.formic.common.datatype.DataTypeName
import de.tu_berlin.formic.common.datatype.client.DataTypeInitiator
import upickle.default._
/**
  * @author Ronny Bräunlich
  */
class FormicBooleanListDataTypeFactory extends FormicLinearDataTypeFactory[Boolean] {

  override def createWrapperType(dataTypeInstanceId: DataTypeInstanceId, dataType: ActorRef): FormicList[Boolean] = {
    new FormicBooleanList(() => {}, null, dataTypeInstanceId, dataType)
  }

  override val name: DataTypeName = FormicBooleanListDataTypeFactory.dataTypeName
}

object FormicBooleanListDataTypeFactory {
  val dataTypeName = DataTypeName("BooleanList")
}
