package de.tu_berlin.formic.datatype.linear.client

import akka.actor.ActorRef
import de.tu_berlin.formic.common.{ClientId, DataStructureInstanceId}
import de.tu_berlin.formic.common.datastructure.DataStructureName
import de.tu_berlin.formic.common.datastructure.client.ClientDataStructureEvent
import upickle.default._
/**
  * @author Ronny Bräunlich
  */
class FormicBooleanListDataStructureFactory extends FormicLinearDataStructureFactory[Boolean] {

  override def createWrapperType(dataTypeInstanceId: DataStructureInstanceId, dataType: ActorRef, localClientId: ClientId): FormicList[Boolean] = {
    new FormicBooleanList((ClientDataTypeEvent) => {}, RemoteDataStructureInitiator, dataTypeInstanceId, dataType, localClientId)
  }

  override val name: DataStructureName = FormicBooleanListDataStructureFactory.name
}

object FormicBooleanListDataStructureFactory {
  val name = DataStructureName("BooleanList")
}
