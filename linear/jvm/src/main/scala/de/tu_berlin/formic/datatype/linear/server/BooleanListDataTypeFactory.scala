package de.tu_berlin.formic.datatype.linear.server

import de.tu_berlin.formic.common.datatype.DataTypeName
import upickle.default._

/**
  * @author Ronny Bräunlich
  */
class BooleanListDataTypeFactory extends LinearDataTypeFactory[Boolean] {
  override val name: DataTypeName = BooleanListDataTypeFactory.name
}

object BooleanListDataTypeFactory {
  val name = DataTypeName("BooleanList")
}
