package de.tu_berlin.formic.datatype.tree.client

import akka.actor.ActorRef
import de.tu_berlin.formic.common.{ClientId, DataTypeInstanceId}
import de.tu_berlin.formic.common.datatype.DataTypeName
import de.tu_berlin.formic.common.datatype.client.ClientDataTypeEvent

/**
  * @author Ronny Bräunlich
  */

class FormicStringTreeFactory extends FormicTreeDataTypeFactory[String] {

  override def createWrapperType(dataTypeInstanceId: DataTypeInstanceId, dataType: ActorRef, localClientId: ClientId): FormicTree[String] = {
    new FormicStringTree((ClientDataTypeEvent) => {}, RemoteDataTypeInitiator, dataTypeInstanceId, dataType, localClientId)
  }

  override val name: DataTypeName = FormicStringTreeFactory.name
}

object FormicStringTreeFactory {

  val name = DataTypeName("StringTree")

}
