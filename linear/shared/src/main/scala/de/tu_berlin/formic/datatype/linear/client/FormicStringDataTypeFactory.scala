package de.tu_berlin.formic.datatype.linear.client

import akka.actor.ActorRef
import de.tu_berlin.formic.common.{ClientId, DataStructureInstanceId$}
import de.tu_berlin.formic.common.datatype.DataStructureName
import de.tu_berlin.formic.common.datatype.client.ClientDataTypeEvent
import upickle.default._

/**
  * @author Ronny Bräunlich
  */
class FormicStringDataTypeFactory extends FormicLinearDataTypeFactory[Char] {

  override def createWrapperType(dataTypeInstanceId: DataStructureInstanceId, dataType: ActorRef, localClientId: ClientId): FormicList[Char] = {
    new FormicString((ClientDataTypeEvent) => {}, RemoteDataTypeInitiator, dataTypeInstanceId, dataType, localClientId)
  }

  override val name: DataStructureName = FormicStringDataTypeFactory.name
}

object FormicStringDataTypeFactory {
  val name = DataStructureName("string")
}