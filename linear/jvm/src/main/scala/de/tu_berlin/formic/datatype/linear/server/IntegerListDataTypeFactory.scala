package de.tu_berlin.formic.datatype.linear.server

import de.tu_berlin.formic.common.datatype.DataTypeName
import upickle.default._

/**
  * @author Ronny Bräunlich
  */
class IntegerListDataTypeFactory extends LinearServerDataTypeFactory[Int] {
  override val name: DataTypeName = IntegerListDataTypeFactory.name
}

object IntegerListDataTypeFactory {
  val name = DataTypeName("IntegerList")
}
