package de.tu_berlin.formic.example

import de.tu_berlin.formic.common.datatype.ServerDataStructureProvider
import de.tu_berlin.formic.datatype.json.JsonServerDataStructureProvider
import de.tu_berlin.formic.datatype.linear.server.LinearServerDataStructureProvider
import de.tu_berlin.formic.datatype.tree.TreeServerDataStructureProvider
import de.tu_berlin.formic.server.ServerDataStructures

/**
  * @author Ronny Bräunlich
  */
trait ExampleServerDataStructures extends ServerDataStructures {
  override val dataStructureProvider: Set[ServerDataStructureProvider] =
    Set(LinearServerDataStructureProvider(), TreeServerDataStructureProvider(), JsonServerDataStructureProvider())
}