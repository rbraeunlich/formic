package de.tu_berlin.formic.client

import de.tu_berlin.formic.common.datatype.ClientDataTypeProvider

/**
  * Cake-pattern trait to be able to initialize a FormicSystem with the data types
  * the user wants.
  *
  * @author Ronny Bräunlich
  */
trait ClientDataTypes {

  val dataTypeProvider: Set[ClientDataTypeProvider]

}
