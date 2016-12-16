package de.tu_berlin.formic.datatype.tree

import de.tu_berlin.formic.common.datatype.DataTypeName

/**
  * @author Ronny Bräunlich
  */
class IntegerTreeDataTypeFactory extends TreeDataTypeFactory[Int] {
  override val name: DataTypeName = IntegerTreeDataTypeFactory.name
}

object IntegerTreeDataTypeFactory {
  val name = DataTypeName("IntegerTree")
}
