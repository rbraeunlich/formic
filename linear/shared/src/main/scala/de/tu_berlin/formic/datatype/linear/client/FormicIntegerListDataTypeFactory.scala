package de.tu_berlin.formic.datatype.linear.client

import akka.actor.ActorRef
import de.tu_berlin.formic.common.{ClientId, DataStructureInstanceId$}
import de.tu_berlin.formic.common.datatype.DataTypeName
import upickle.default._

/**
  * @author Ronny Bräunlich
  */
class FormicIntegerListDataTypeFactory extends FormicLinearDataTypeFactory[Int] {

  override def createWrapperType(dataTypeInstanceId: DataStructureInstanceId, dataType: ActorRef, localClientId: ClientId): FormicList[Int] = {
    new FormicIntegerList((ClientDataTypeEvent) => {}, RemoteDataTypeInitiator, dataTypeInstanceId, dataType, localClientId)
  }

  override val name: DataTypeName = FormicIntegerListDataTypeFactory.name
}

object FormicIntegerListDataTypeFactory {
  val name = DataTypeName("IntegerList")
}