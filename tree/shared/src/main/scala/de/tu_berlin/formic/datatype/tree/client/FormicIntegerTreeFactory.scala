package de.tu_berlin.formic.datatype.tree.client

import akka.actor.ActorRef
import de.tu_berlin.formic.common.DataTypeInstanceId
import de.tu_berlin.formic.common.datatype.DataTypeName

/**
  * @author Ronny Bräunlich
  */

class FormicIntegerTreeFactory extends FormicTreeDataTypeFactory[Int] {

  override def createWrapperType(dataTypeInstanceId: DataTypeInstanceId, dataType: ActorRef): FormicTree[Int] = {
    new FormicIntegerTree(() => {}, RemoteDataTypeInitiator, dataTypeInstanceId, dataType)
  }

  override val name: DataTypeName = FormicIntegerTreeFactory.name
}

object FormicIntegerTreeFactory {
  val name = DataTypeName("IntegerTree")
}
