package de.tu_berlin.formic.datastructure.tree.client

import akka.actor.ActorRef
import de.tu_berlin.formic.common.datastructure.DataStructureName
import de.tu_berlin.formic.common.{ClientId, DataStructureInstanceId}

/**
  * @author Ronny Bräunlich
  */

class FormicStringTreeFactory extends FormicTreeDataStructureFactory[String] {

  override def createWrapper(dataStructureInstanceId: DataStructureInstanceId, dataStructure: ActorRef, localClientId: ClientId): FormicTree[String] = {
    new FormicStringTree((ClientDataStructureEvent) => {}, RemoteDataStructureInitiator, dataStructureInstanceId, dataStructure, localClientId)
  }

  override val name: DataStructureName = FormicStringTreeFactory.name
}

object FormicStringTreeFactory {

  val name = DataStructureName("StringTree")

}
