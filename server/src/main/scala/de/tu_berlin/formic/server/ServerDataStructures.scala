package de.tu_berlin.formic.server

import akka.actor.ActorRef
import de.tu_berlin.formic.common.datatype.{DataStructureName, ServerDataStructureProvider}

/**
  * Cake-pattern trait to be able to initialize a FormicServer with the data structures
  * the user wants.
  *
  * @author Ronny Bräunlich
  */
trait ServerDataStructures {

  val dataStructureProvider: Set[ServerDataStructureProvider]

}
