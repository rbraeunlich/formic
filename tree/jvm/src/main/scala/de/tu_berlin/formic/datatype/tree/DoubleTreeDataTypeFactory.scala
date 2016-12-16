package de.tu_berlin.formic.datatype.tree

import de.tu_berlin.formic.common.datatype.DataTypeName

/**
  * @author Ronny Bräunlich
  */
class DoubleTreeDataTypeFactory extends TreeDataTypeFactory[Double] {
  override val name: DataTypeName = DoubleTreeDataTypeFactory.name
}

object DoubleTreeDataTypeFactory {
  val name = DataTypeName("DoubleTree")
}