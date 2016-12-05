package de.tu_berlin.formic.datatype.linear.server

import de.tu_berlin.formic.common.datatype.DataTypeName
import upickle.default._

/**
  * @author Ronny Bräunlich
  */
class DoubleListDataTypeFactory extends LinearDataTypeFactory[Double] {
  override val name: DataTypeName = DoubleListDataTypeFactory.name
}

object DoubleListDataTypeFactory {
  val name = DataTypeName("DoubleList")
}
