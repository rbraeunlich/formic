package de.tu_berlin.formic.client

import de.tu_berlin.formic.common.datastructure.ClientDataStructureProvider

/**
  * Cake-pattern trait to be able to initialize a FormicSystem with the data structures
  * the user wants.
  *
  * @author Ronny Bräunlich
  */
trait ClientDataStructures {

  val dataStructureProvider: Set[ClientDataStructureProvider]

}
