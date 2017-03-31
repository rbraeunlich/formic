package de.tu_berlin.formic.datatype.linear.server

import de.tu_berlin.formic.common.datatype.DataStructureName
import upickle.default._

/**
  * @author Ronny Bräunlich
  */
class BooleanListDataStructureFactory extends LinearServerDataStructureFactory[Boolean] {
  override val name: DataStructureName = BooleanListDataStructureFactory.name
}

object BooleanListDataStructureFactory {
  val name = DataStructureName("BooleanList")
}
