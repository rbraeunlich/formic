package de.tu_berlin.formic.datatype.linear.client

import akka.actor.ActorRef
import de.tu_berlin.formic.common.{ClientId, DataStructureInstanceId}
import de.tu_berlin.formic.common.datatype.DataStructureName
import de.tu_berlin.formic.common.datatype.client.ClientDataTypeEvent
import upickle.default._

/**
  * @author Ronny Bräunlich
  */
class FormicDoubleListDataTypeFactory extends FormicLinearDataTypeFactory[Double] {

  override def createWrapperType(dataTypeInstanceId: DataStructureInstanceId, dataType: ActorRef, localClientId: ClientId): FormicList[Double] = {
    new FormicDoubleList((ClientDataTypeEvent) => {}, RemoteDataStructureInitiator, dataTypeInstanceId, dataType, localClientId)
  }

  override val name: DataStructureName = FormicDoubleListDataTypeFactory.name
}

object FormicDoubleListDataTypeFactory {
  val name = DataStructureName("DoubleList")
}