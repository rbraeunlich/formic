package de.tu_berlin.formic.datatype.tree.client

import akka.actor.ActorRef
import de.tu_berlin.formic.common.{ClientId, DataTypeInstanceId}
import de.tu_berlin.formic.common.datatype.DataTypeName

/**
  * @author Ronny Bräunlich
  */

class FormicDoubleTreeFactory extends FormicTreeDataTypeFactory[Double] {

  override def createWrapperType(dataTypeInstanceId: DataTypeInstanceId, dataType: ActorRef, localClientId: ClientId): FormicTree[Double] = {
    new FormicDoubleTree(() => {}, RemoteDataTypeInitiator, dataTypeInstanceId, dataType, localClientId)
  }

  override val name: DataTypeName = FormicDoubleTreeFactory.name
}

object FormicDoubleTreeFactory {
  val name: DataTypeName = DataTypeName("DoubleTree")

}
