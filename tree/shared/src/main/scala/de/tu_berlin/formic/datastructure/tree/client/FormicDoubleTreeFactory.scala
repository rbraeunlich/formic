package de.tu_berlin.formic.datastructure.tree.client

import akka.actor.ActorRef
import de.tu_berlin.formic.common.datastructure.DataStructureName
import de.tu_berlin.formic.common.{ClientId, DataStructureInstanceId}

/**
  * @author Ronny Bräunlich
  */

class FormicDoubleTreeFactory extends FormicTreeDataStructureFactory[Double] {

  override def createWrapperType(dataTypeInstanceId: DataStructureInstanceId, dataType: ActorRef, localClientId: ClientId): FormicTree[Double] = {
    new FormicDoubleTree((ClientDataTypeEvent) => {}, RemoteDataStructureInitiator, dataTypeInstanceId, dataType, localClientId)
  }

  override val name: DataStructureName = FormicDoubleTreeFactory.name
}

object FormicDoubleTreeFactory {
  val name: DataStructureName = DataStructureName("DoubleTree")

}
