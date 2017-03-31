package de.tu_berlin.formic.datatype.linear.server

import de.tu_berlin.formic.common.datastructure.DataStructureName
import upickle.default._

/**
  * @author Ronny Bräunlich
  */
class DoubleListDataStructureFactory extends LinearServerDataStructureFactory[Double] {
  override val name: DataStructureName = DoubleListDataStructureFactory.name
}

object DoubleListDataStructureFactory {
  val name = DataStructureName("DoubleList")
}
