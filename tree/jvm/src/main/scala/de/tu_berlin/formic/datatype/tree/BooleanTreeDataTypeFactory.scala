package de.tu_berlin.formic.datatype.tree

import de.tu_berlin.formic.common.datatype.DataTypeName

/**
  * @author Ronny Bräunlich
  */
class BooleanTreeDataTypeFactory extends TreeDataTypeFactory[Boolean] {
  override val name: DataTypeName = BooleanTreeDataTypeFactory.name
}

object BooleanTreeDataTypeFactory {
  val name = DataTypeName("BooleanTree")
}