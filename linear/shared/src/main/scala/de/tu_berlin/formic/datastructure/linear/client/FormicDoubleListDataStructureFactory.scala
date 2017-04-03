package de.tu_berlin.formic.datastructure.linear.client

import akka.actor.ActorRef
import de.tu_berlin.formic.common.datastructure.DataStructureName
import de.tu_berlin.formic.common.{ClientId, DataStructureInstanceId}
import upickle.default._

/**
  * @author Ronny Bräunlich
  */
class FormicDoubleListDataStructureFactory extends FormicLinearDataStructureFactory[Double] {

  override def createWrapperType(dataTypeInstanceId: DataStructureInstanceId, dataType: ActorRef, localClientId: ClientId): FormicList[Double] = {
    new FormicDoubleList((ClientDataTypeEvent) => {}, RemoteDataStructureInitiator, dataTypeInstanceId, dataType, localClientId)
  }

  override val name: DataStructureName = FormicDoubleListDataStructureFactory.name
}

object FormicDoubleListDataStructureFactory {
  val name = DataStructureName("DoubleList")
}