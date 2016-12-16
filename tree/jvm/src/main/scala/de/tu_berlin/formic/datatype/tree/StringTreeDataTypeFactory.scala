package de.tu_berlin.formic.datatype.tree

import de.tu_berlin.formic.common.datatype.DataTypeName

/**
  * @author Ronny Bräunlich
  */
class StringTreeDataTypeFactory extends TreeDataTypeFactory[String] {
  override val name: DataTypeName = StringTreeDataTypeFactory.name
}

object StringTreeDataTypeFactory {
  val name = DataTypeName("StringTree")
}