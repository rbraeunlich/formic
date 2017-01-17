package de.tu_berlin.formic.datatype.linear.client

import akka.actor.ActorRef
import de.tu_berlin.formic.common.{ClientId, DataTypeInstanceId}
import de.tu_berlin.formic.common.datatype.DataTypeName
import upickle.default._
/**
  * @author Ronny Bräunlich
  */
class FormicBooleanListDataTypeFactory extends FormicLinearDataTypeFactory[Boolean] {

  override def createWrapperType(dataTypeInstanceId: DataTypeInstanceId, dataType: ActorRef, localClientId: ClientId): FormicList[Boolean] = {
    new FormicBooleanList(() => {}, RemoteDataTypeInitiator, dataTypeInstanceId, dataType, localClientId)
  }

  override val name: DataTypeName = FormicBooleanListDataTypeFactory.name
}

object FormicBooleanListDataTypeFactory {
  val name = DataTypeName("BooleanList")
}
