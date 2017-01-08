package de.tu_berlin.formic.datatype.linear.client

import akka.actor.ActorRef
import de.tu_berlin.formic.common.DataTypeInstanceId
import de.tu_berlin.formic.common.datatype.DataTypeName
import upickle.default._

/**
  * @author Ronny Bräunlich
  */
class FormicDoubleListDataTypeFactory extends FormicLinearDataTypeFactory[Double] {

  override def createWrapperType(dataTypeInstanceId: DataTypeInstanceId, dataType: ActorRef): FormicList[Double] = {
    new FormicDoubleList(() => {}, RemoteDataTypeInitiator, dataTypeInstanceId, dataType)
  }

  override val name: DataTypeName = FormicDoubleListDataTypeFactory.dataTypeName
}

object FormicDoubleListDataTypeFactory {
  val dataTypeName = DataTypeName("DoubleList")
}