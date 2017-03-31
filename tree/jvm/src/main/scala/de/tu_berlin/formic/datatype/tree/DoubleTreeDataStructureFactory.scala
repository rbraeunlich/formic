package de.tu_berlin.formic.datatype.tree

import de.tu_berlin.formic.common.datastructure.DataStructureName

/**
  * @author Ronny Bräunlich
  */
class DoubleTreeDataStructureFactory extends TreeDataStructureFactory[Double] {
  override val name: DataStructureName = DoubleTreeDataStructureFactory.name
}

object DoubleTreeDataStructureFactory {
  val name = DataStructureName("DoubleTree")
}