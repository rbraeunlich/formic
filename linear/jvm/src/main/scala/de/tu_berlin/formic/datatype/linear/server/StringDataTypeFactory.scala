package de.tu_berlin.formic.datatype.linear.server

import de.tu_berlin.formic.common.datatype.DataTypeName
import upickle.default._

/**
  * @author Ronny Bräunlich
  */
class StringDataTypeFactory extends LinearDataTypeFactory[Char] {
  override val name: DataTypeName = StringDataTypeFactory.name
}

object StringDataTypeFactory {
  val name = DataTypeName("string")
}