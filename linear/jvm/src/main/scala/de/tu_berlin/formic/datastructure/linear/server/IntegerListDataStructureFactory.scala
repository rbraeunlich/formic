package de.tu_berlin.formic.datastructure.linear.server

import de.tu_berlin.formic.common.datastructure.DataStructureName
import upickle.default._

/**
  * @author Ronny Bräunlich
  */
class IntegerListDataStructureFactory extends LinearServerDataStructureFactory[Int] {
  override val name: DataStructureName = IntegerListDataStructureFactory.name
}

object IntegerListDataStructureFactory {
  val name = DataStructureName("IntegerList")
}
