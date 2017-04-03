package de.tu_berlin.formic.datastructure.linear.client

import akka.actor.ActorRef
import de.tu_berlin.formic.common.{ClientId, DataStructureInstanceId}
import de.tu_berlin.formic.common.datastructure.DataStructureName
import upickle.default._

/**
  * @author Ronny Bräunlich
  */
class FormicIntegerListDataStructureFactory extends FormicLinearDataStructureFactory[Int] {

  override def createWrapperType(dataTypeInstanceId: DataStructureInstanceId, dataType: ActorRef, localClientId: ClientId): FormicList[Int] = {
    new FormicIntegerList((ClientDataTypeEvent) => {}, RemoteDataStructureInitiator, dataTypeInstanceId, dataType, localClientId)
  }

  override val name: DataStructureName = FormicIntegerListDataStructureFactory.name
}

object FormicIntegerListDataStructureFactory {
  val name = DataStructureName("IntegerList")
}