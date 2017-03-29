package de.tu_berlin.formic.datatype.linear.client

import akka.actor.ActorRef
import de.tu_berlin.formic.common.{ClientId, DataStructureInstanceId}
import de.tu_berlin.formic.common.datatype.DataStructureName
import de.tu_berlin.formic.common.datatype.client.ClientDataTypeEvent
import upickle.default._
/**
  * @author Ronny Bräunlich
  */
class FormicBooleanListDataTypeFactory extends FormicLinearDataTypeFactory[Boolean] {

  override def createWrapperType(dataTypeInstanceId: DataStructureInstanceId, dataType: ActorRef, localClientId: ClientId): FormicList[Boolean] = {
    new FormicBooleanList((ClientDataTypeEvent) => {}, RemoteDataTypeInitiator, dataTypeInstanceId, dataType, localClientId)
  }

  override val name: DataStructureName = FormicBooleanListDataTypeFactory.name
}

object FormicBooleanListDataTypeFactory {
  val name = DataStructureName("BooleanList")
}
