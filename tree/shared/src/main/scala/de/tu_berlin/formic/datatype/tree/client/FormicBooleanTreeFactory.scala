package de.tu_berlin.formic.datatype.tree.client

import akka.actor.ActorRef
import de.tu_berlin.formic.common.{ClientId, DataStructureInstanceId$}
import de.tu_berlin.formic.common.datatype.DataTypeName
import de.tu_berlin.formic.common.datatype.client.ClientDataTypeEvent
import upickle.default._

/**
  * @author Ronny Bräunlich
  */
class FormicBooleanTreeFactory extends FormicTreeDataTypeFactory[Boolean] {

  override def createWrapperType(dataTypeInstanceId: DataStructureInstanceId, dataType: ActorRef, localClientId: ClientId): FormicTree[Boolean] = {
    new FormicBooleanTree((ClientDataTypeEvent) => {}, RemoteDataTypeInitiator, dataTypeInstanceId, dataType, localClientId)
  }

  override val name: DataTypeName = FormicBooleanTreeFactory.name
}

object FormicBooleanTreeFactory {
  val name = DataTypeName("BooleanTree")
}
