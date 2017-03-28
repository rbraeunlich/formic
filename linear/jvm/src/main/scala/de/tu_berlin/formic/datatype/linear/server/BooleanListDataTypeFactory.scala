package de.tu_berlin.formic.datatype.linear.server

import de.tu_berlin.formic.common.datatype.DataStructureName
import upickle.default._

/**
  * @author Ronny Bräunlich
  */
class BooleanListDataTypeFactory extends LinearServerDataTypeFactory[Boolean] {
  override val name: DataStructureName = BooleanListDataTypeFactory.name
}

object BooleanListDataTypeFactory {
  val name = DataStructureName("BooleanList")
}
