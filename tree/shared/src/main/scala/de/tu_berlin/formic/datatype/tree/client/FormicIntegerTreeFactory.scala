package de.tu_berlin.formic.datatype.tree.client

import akka.actor.ActorRef
import de.tu_berlin.formic.common.{ClientId, DataStructureInstanceId}
import de.tu_berlin.formic.common.datatype.DataStructureName
import de.tu_berlin.formic.common.datatype.client.ClientDataTypeEvent

/**
  * @author Ronny Bräunlich
  */

class FormicIntegerTreeFactory extends FormicTreeDataTypeFactory[Int] {

  override def createWrapperType(dataTypeInstanceId: DataStructureInstanceId, dataType: ActorRef, localClientId: ClientId): FormicTree[Int] = {
    new FormicIntegerTree((ClientDataTypeEvent) => {}, RemoteDataTypeInitiator, dataTypeInstanceId, dataType, localClientId)
  }

  override val name: DataStructureName = FormicIntegerTreeFactory.name
}

object FormicIntegerTreeFactory {
  val name = DataStructureName("IntegerTree")
}
