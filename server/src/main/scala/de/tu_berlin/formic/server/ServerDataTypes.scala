package de.tu_berlin.formic.server

import akka.actor.ActorRef
import de.tu_berlin.formic.common.datatype.{DataTypeName, ServerDataTypeProvider}

/**
  * Cake-pattern trait to be able to initialize a FormicServer with the data types
  * the user wants.
  *
  * @author Ronny Bräunlich
  */
trait ServerDataTypes {

  val dataTypeProvider: Set[ServerDataTypeProvider]

}
