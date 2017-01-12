package de.tu_berlin.formic.datatype.linear.client

import akka.actor.ActorRef
import de.tu_berlin.formic.common.{ClientId, DataTypeInstanceId}
import de.tu_berlin.formic.common.datatype.DataTypeName
import upickle.default._

/**
  * @author Ronny Bräunlich
  */
class FormicIntegerListDataTypeFactory extends FormicLinearDataTypeFactory[Int] {

  override def createWrapperType(dataTypeInstanceId: DataTypeInstanceId, dataType: ActorRef, localClientId: ClientId): FormicList[Int] = {
    new FormicIntegerList(() => {}, RemoteDataTypeInitiator, dataTypeInstanceId, dataType, localClientId)
  }

  override val name: DataTypeName = FormicIntegerListDataTypeFactory.dataTypeName
}

object FormicIntegerListDataTypeFactory {
  val dataTypeName = DataTypeName("IntegerList")
}