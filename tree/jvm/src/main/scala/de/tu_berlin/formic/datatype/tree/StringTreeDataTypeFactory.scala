package de.tu_berlin.formic.datatype.tree

import de.tu_berlin.formic.common.datatype.DataStructureName

/**
  * @author Ronny Bräunlich
  */
class StringTreeDataTypeFactory extends TreeDataTypeFactory[String] {
  override val name: DataStructureName = StringTreeDataTypeFactory.name
}

object StringTreeDataTypeFactory {
  val name = DataStructureName("StringTree")
}