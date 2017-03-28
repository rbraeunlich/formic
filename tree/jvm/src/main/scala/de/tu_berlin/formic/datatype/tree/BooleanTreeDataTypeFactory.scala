package de.tu_berlin.formic.datatype.tree

import de.tu_berlin.formic.common.datatype.DataStructureName

/**
  * @author Ronny Bräunlich
  */
class BooleanTreeDataTypeFactory extends TreeDataTypeFactory[Boolean] {
  override val name: DataStructureName = BooleanTreeDataTypeFactory.name
}

object BooleanTreeDataTypeFactory {
  val name = DataStructureName("BooleanTree")
}