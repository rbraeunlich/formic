package de.tu_berlin.formic.datatype.tree.client

import akka.actor.ActorRef
import de.tu_berlin.formic.common.{ClientId, DataTypeInstanceId}
import de.tu_berlin.formic.common.datatype.DataTypeName
import upickle.default._

/**
  * @author Ronny Bräunlich
  */
class FormicBooleanTreeFactory extends FormicTreeDataTypeFactory[Boolean] {

  override def createWrapperType(dataTypeInstanceId: DataTypeInstanceId, dataType: ActorRef, localClientId: ClientId): FormicTree[Boolean] = {
    new FormicBooleanTree(() => {}, RemoteDataTypeInitiator, dataTypeInstanceId, dataType, localClientId)
  }

  override val name: DataTypeName = FormicBooleanTreeFactory.name
}

object FormicBooleanTreeFactory {
  val name = DataTypeName("BooleanTree")
}
