package de.tu_berlin.formic.datatype.tree

import de.tu_berlin.formic.common.datatype.DataStructureName

/**
  * @author Ronny Bräunlich
  */
class IntegerTreeDataTypeFactory extends TreeDataTypeFactory[Int] {
  override val name: DataStructureName = IntegerTreeDataTypeFactory.name
}

object IntegerTreeDataTypeFactory {
  val name = DataStructureName("IntegerTree")
}
