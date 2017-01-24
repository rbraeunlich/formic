package de.tu_berlin.formic.datatype.linear.client

import akka.actor.ActorRef
import de.tu_berlin.formic.common.{ClientId, DataTypeInstanceId}
import de.tu_berlin.formic.common.datatype.DataTypeName
import de.tu_berlin.formic.common.datatype.client.ClientDataTypeEvent
import upickle.default._

/**
  * @author Ronny Bräunlich
  */
class FormicDoubleListDataTypeFactory extends FormicLinearDataTypeFactory[Double] {

  override def createWrapperType(dataTypeInstanceId: DataTypeInstanceId, dataType: ActorRef, localClientId: ClientId): FormicList[Double] = {
    new FormicDoubleList((ClientDataTypeEvent) => {}, RemoteDataTypeInitiator, dataTypeInstanceId, dataType, localClientId)
  }

  override val name: DataTypeName = FormicDoubleListDataTypeFactory.name
}

object FormicDoubleListDataTypeFactory {
  val name = DataTypeName("DoubleList")
}