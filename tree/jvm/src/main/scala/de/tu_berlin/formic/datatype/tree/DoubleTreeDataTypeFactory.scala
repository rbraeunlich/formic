package de.tu_berlin.formic.datatype.tree

import de.tu_berlin.formic.common.datatype.DataStructureName

/**
  * @author Ronny Bräunlich
  */
class DoubleTreeDataTypeFactory extends TreeDataTypeFactory[Double] {
  override val name: DataStructureName = DoubleTreeDataTypeFactory.name
}

object DoubleTreeDataTypeFactory {
  val name = DataStructureName("DoubleTree")
}