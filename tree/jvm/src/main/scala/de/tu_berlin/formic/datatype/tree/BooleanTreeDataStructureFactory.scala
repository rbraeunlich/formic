package de.tu_berlin.formic.datatype.tree

import de.tu_berlin.formic.common.datastructure.DataStructureName

/**
  * @author Ronny Bräunlich
  */
class BooleanTreeDataStructureFactory extends TreeDataStructureFactory[Boolean] {
  override val name: DataStructureName = BooleanTreeDataStructureFactory.name
}

object BooleanTreeDataStructureFactory {
  val name = DataStructureName("BooleanTree")
}