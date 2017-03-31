package de.tu_berlin.formic.datatype.linear.server

import de.tu_berlin.formic.common.datastructure.DataStructureName
import upickle.default._

/**
  * @author Ronny Bräunlich
  */
class StringDataStructureFactory extends LinearServerDataStructureFactory[Char] {
  override val name: DataStructureName = StringDataStructureFactory.name
}

object StringDataStructureFactory {
  val name = DataStructureName("string")
}