package de.tu_berlin.formic.datatype.tree.client

import akka.actor.ActorRef
import de.tu_berlin.formic.common.{ClientId, DataStructureInstanceId}
import de.tu_berlin.formic.common.datastructure.DataStructureName
import de.tu_berlin.formic.common.datastructure.client.ClientDataStructureEvent
import upickle.default._

/**
  * @author Ronny Bräunlich
  */
class FormicBooleanTreeFactory extends FormicTreeDataStructureFactory[Boolean] {

  override def createWrapperType(dataTypeInstanceId: DataStructureInstanceId, dataType: ActorRef, localClientId: ClientId): FormicTree[Boolean] = {
    new FormicBooleanTree((ClientDataTypeEvent) => {}, RemoteDataStructureInitiator, dataTypeInstanceId, dataType, localClientId)
  }

  override val name: DataStructureName = FormicBooleanTreeFactory.name
}

object FormicBooleanTreeFactory {
  val name = DataStructureName("BooleanTree")
}
