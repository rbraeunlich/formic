package de.tu_berlin.formic.example

import de.tu_berlin.formic.client.ClientDataTypes
import de.tu_berlin.formic.common.datatype.ClientDataTypeProvider
import de.tu_berlin.formic.datatype.json.client.JsonClientDataTypeProvider
import de.tu_berlin.formic.datatype.linear.client.LinearClientDataTypeProvider
import de.tu_berlin.formic.datatype.tree.client.TreeClientDataTypeProvider

/**
  * @author Ronny Bräunlich
  */
trait ExampleClientDataTypes extends ClientDataTypes {
  override val dataTypeProvider: Set[ClientDataTypeProvider] = Set(
    LinearClientDataTypeProvider(), TreeClientDataTypeProvider(), JsonClientDataTypeProvider()
  )
}
