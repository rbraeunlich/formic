package de.tu_berlin.formic.example

import de.tu_berlin.formic.common.datatype.ServerDataTypeProvider
import de.tu_berlin.formic.datatype.json.JsonServerDataTypeProvider
import de.tu_berlin.formic.datatype.linear.server.LinearServerDataTypeProvider
import de.tu_berlin.formic.datatype.tree.TreeServerDataTypeProvider
import de.tu_berlin.formic.server.ServerDataTypes

/**
  * @author Ronny Bräunlich
  */
trait ExampleServerDataTypes extends ServerDataTypes {
  override val dataTypeProvider: Set[ServerDataTypeProvider] =
    Set(LinearServerDataTypeProvider(), TreeServerDataTypeProvider(), JsonServerDataTypeProvider())
}
