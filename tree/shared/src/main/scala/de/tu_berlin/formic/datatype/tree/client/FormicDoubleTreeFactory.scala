package de.tu_berlin.formic.datatype.tree.client

import akka.actor.ActorRef
import de.tu_berlin.formic.common.DataTypeInstanceId
import de.tu_berlin.formic.common.datatype.DataTypeName

/**
  * @author Ronny Bräunlich
  */

class FormicDoubleTreeFactory extends FormicTreeDataTypeFactory[Double] {

  override def createWrapperType(dataTypeInstanceId: DataTypeInstanceId, dataType: ActorRef): FormicTree[Double] = {
    new FormicDoubleTree(() => {}, RemoteDataTypeInitiator, dataTypeInstanceId, dataType)
  }

  override val name: DataTypeName = FormicDoubleTreeFactory.name
}

object FormicDoubleTreeFactory {
  val name: DataTypeName = DataTypeName("DoubleTree")

}
