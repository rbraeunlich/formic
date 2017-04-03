package de.tu_berlin.formic.example

import de.tu_berlin.formic.common.datastructure.ServerDataStructureProvider
import de.tu_berlin.formic.datastructure.json.JsonServerDataStructureProvider
import de.tu_berlin.formic.datastructure.linear.server.LinearServerDataStructureProvider
import de.tu_berlin.formic.datastructure.tree.TreeServerDataStructureProvider
import de.tu_berlin.formic.server.ServerDataStructures

/**
  * @author Ronny Bräunlich
  */
trait ExampleServerDataStructures extends ServerDataStructures {
  override val dataStructureProvider: Set[ServerDataStructureProvider] =
    Set(LinearServerDataStructureProvider(), TreeServerDataStructureProvider(), JsonServerDataStructureProvider())
}
