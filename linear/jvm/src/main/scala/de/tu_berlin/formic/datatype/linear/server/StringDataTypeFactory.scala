package de.tu_berlin.formic.datatype.linear.server

import de.tu_berlin.formic.common.datatype.DataStructureName
import upickle.default._

/**
  * @author Ronny Bräunlich
  */
class StringDataTypeFactory extends LinearServerDataTypeFactory[Char] {
  override val name: DataStructureName = StringDataTypeFactory.name
}

object StringDataTypeFactory {
  val name = DataStructureName("string")
}