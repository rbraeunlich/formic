package de.tu_berlin.formic.datastructure.tree

import de.tu_berlin.formic.common.datastructure.DataStructureName

/**
  * @author Ronny Bräunlich
  */
class IntegerTreeDataStructureFactory extends TreeDataStructureFactory[Int] {
  override val name: DataStructureName = IntegerTreeDataStructureFactory.name
}

object IntegerTreeDataStructureFactory {
  val name = DataStructureName("IntegerTree")
}
