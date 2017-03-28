package de.tu_berlin.formic.datatype.linear.server

import de.tu_berlin.formic.common.datatype.DataStructureName
import upickle.default._

/**
  * @author Ronny Bräunlich
  */
class DoubleListDataTypeFactory extends LinearServerDataTypeFactory[Double] {
  override val name: DataStructureName = DoubleListDataTypeFactory.name
}

object DoubleListDataTypeFactory {
  val name = DataStructureName("DoubleList")
}
