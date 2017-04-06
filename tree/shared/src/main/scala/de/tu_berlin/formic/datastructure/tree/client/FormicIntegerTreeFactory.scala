package de.tu_berlin.formic.datastructure.tree.client

import akka.actor.ActorRef
import de.tu_berlin.formic.common.datastructure.DataStructureName
import de.tu_berlin.formic.common.{ClientId, DataStructureInstanceId}

/**
  * @author Ronny Bräunlich
  */

class FormicIntegerTreeFactory extends FormicTreeDataStructureFactory[Int] {

  override def createWrapper(dataStructureInstanceId: DataStructureInstanceId, dataStructure: ActorRef, localClientId: ClientId): FormicTree[Int] = {
    new FormicIntegerTree((ClientDataStructureEvent) => {}, RemoteDataStructureInitiator, dataStructureInstanceId, dataStructure, localClientId)
  }

  override val name: DataStructureName = FormicIntegerTreeFactory.name
}

object FormicIntegerTreeFactory {
  val name = DataStructureName("IntegerTree")
}
