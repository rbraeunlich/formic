package de.tu_berlin.formic.datatype.tree

import de.tu_berlin.formic.common.datatype.DataStructureName

/**
  * @author Ronny Bräunlich
  */
class StringTreeDataStructureFactory extends TreeDataStructureFactory[String] {
  override val name: DataStructureName = StringTreeDataStructureFactory.name
}

object StringTreeDataStructureFactory {
  val name = DataStructureName("StringTree")
}