package de.tu_berlin.formic.example

import de.tu_berlin.formic.client.ClientDataStructures
import de.tu_berlin.formic.common.datastructure.ClientDataStructureProvider
import de.tu_berlin.formic.datastructure.json.client.JsonClientDataStructureProvider
import de.tu_berlin.formic.datastructure.linear.client.LinearClientDataStructureProvider
import de.tu_berlin.formic.datastructure.tree.client.TreeClientDataStructureProvider

/**
  * @author Ronny Bräunlich
  */
trait ExampleClientDataStructures extends ClientDataStructures {
  override val dataStructureProvider: Set[ClientDataStructureProvider] = Set(
    LinearClientDataStructureProvider(), TreeClientDataStructureProvider(), JsonClientDataStructureProvider()
  )
}
