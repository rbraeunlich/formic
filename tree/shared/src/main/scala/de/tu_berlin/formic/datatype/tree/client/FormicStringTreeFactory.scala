package de.tu_berlin.formic.datatype.tree.client

import akka.actor.ActorRef
import de.tu_berlin.formic.common.{ClientId, DataStructureInstanceId$}
import de.tu_berlin.formic.common.datatype.DataTypeName
import de.tu_berlin.formic.common.datatype.client.ClientDataTypeEvent

/**
  * @author Ronny Bräunlich
  */

class FormicStringTreeFactory extends FormicTreeDataTypeFactory[String] {

  override def createWrapperType(dataTypeInstanceId: DataStructureInstanceId, dataType: ActorRef, localClientId: ClientId): FormicTree[String] = {
    new FormicStringTree((ClientDataTypeEvent) => {}, RemoteDataTypeInitiator, dataTypeInstanceId, dataType, localClientId)
  }

  override val name: DataTypeName = FormicStringTreeFactory.name
}

object FormicStringTreeFactory {

  val name = DataTypeName("StringTree")

}
