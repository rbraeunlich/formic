package de.tu_berlin.formic.datatype.tree.client

import akka.actor.ActorRef
import de.tu_berlin.formic.common.{ClientId, DataTypeInstanceId}
import de.tu_berlin.formic.common.datatype.DataTypeName
import de.tu_berlin.formic.common.datatype.client.ClientDataTypeEvent

/**
  * @author Ronny Bräunlich
  */

class FormicIntegerTreeFactory extends FormicTreeDataTypeFactory[Int] {

  override def createWrapperType(dataTypeInstanceId: DataTypeInstanceId, dataType: ActorRef, localClientId: ClientId): FormicTree[Int] = {
    new FormicIntegerTree((ClientDataTypeEvent) => {}, RemoteDataTypeInitiator, dataTypeInstanceId, dataType, localClientId)
  }

  override val name: DataTypeName = FormicIntegerTreeFactory.name
}

object FormicIntegerTreeFactory {
  val name = DataTypeName("IntegerTree")
}
