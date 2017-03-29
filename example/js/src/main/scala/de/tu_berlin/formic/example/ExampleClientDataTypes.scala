package de.tu_berlin.formic.example

import de.tu_berlin.formic.client.ClientDataTypes
import de.tu_berlin.formic.common.datatype.ClientDataStructureProvider
import de.tu_berlin.formic.datatype.json.client.JsonClientDataStructureProvider
import de.tu_berlin.formic.datatype.linear.client.LinearClientDataStructureProvider
import de.tu_berlin.formic.datatype.tree.client.TreeClientDataStructureProvider

/**
  * @author Ronny Bräunlich
  */
trait ExampleClientDataTypes extends ClientDataTypes {
  override val dataTypeProvider: Set[ClientDataStructureProvider] = Set(
    LinearClientDataStructureProvider(), TreeClientDataStructureProvider(), JsonClientDataStructureProvider()
  )
}
