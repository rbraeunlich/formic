package de.tu_berlin.formic.datastructure.tree.client

import akka.actor.ActorRef
import de.tu_berlin.formic.common.datastructure.DataStructureName
import de.tu_berlin.formic.common.{ClientId, DataStructureInstanceId}

/**
  * @author Ronny Bräunlich
  */

class FormicStringTreeFactory extends FormicTreeDataStructureFactory[String] {

  override def createWrapper(dataTypeInstanceId: DataStructureInstanceId, dataType: ActorRef, localClientId: ClientId): FormicTree[String] = {
    new FormicStringTree((ClientDataTypeEvent) => {}, RemoteDataStructureInitiator, dataTypeInstanceId, dataType, localClientId)
  }

  override val name: DataStructureName = FormicStringTreeFactory.name
}

object FormicStringTreeFactory {

  val name = DataStructureName("StringTree")

}
