package de.tu_berlin.formic.example

import de.tu_berlin.formic.client.ClientDataStructures
import de.tu_berlin.formic.common.datatype.ClientDataStructureProvider
import de.tu_berlin.formic.datatype.json.client.JsonClientDataStructureProvider
import de.tu_berlin.formic.datatype.linear.client.LinearClientDataStructureProvider
import de.tu_berlin.formic.datatype.tree.client.TreeClientDataStructureProvider

/**
  * @author Ronny Bräunlich
  */
trait ExampleClientDataStructures extends ClientDataStructures {
  override val dataStructureProvider: Set[ClientDataStructureProvider] = Set(
    LinearClientDataStructureProvider(), TreeClientDataStructureProvider(), JsonClientDataStructureProvider()
  )
}
