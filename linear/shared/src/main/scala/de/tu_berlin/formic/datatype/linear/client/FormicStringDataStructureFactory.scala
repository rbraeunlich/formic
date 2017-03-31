package de.tu_berlin.formic.datatype.linear.client

import akka.actor.ActorRef
import de.tu_berlin.formic.common.{ClientId, DataStructureInstanceId}
import de.tu_berlin.formic.common.datastructure.DataStructureName
import de.tu_berlin.formic.common.datastructure.client.ClientDataStructureEvent
import upickle.default._

/**
  * @author Ronny Bräunlich
  */
class FormicStringDataStructureFactory extends FormicLinearDataStructureFactory[Char] {

  override def createWrapperType(dataTypeInstanceId: DataStructureInstanceId, dataType: ActorRef, localClientId: ClientId): FormicList[Char] = {
    new FormicString((ClientDataTypeEvent) => {}, RemoteDataStructureInitiator, dataTypeInstanceId, dataType, localClientId)
  }

  override val name: DataStructureName = FormicStringDataStructureFactory.name
}

object FormicStringDataStructureFactory {
  val name = DataStructureName("string")
}