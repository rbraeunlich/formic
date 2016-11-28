package de.tu_berlin.formic.common.datatype

import akka.actor.ActorRef

/**
  * This interface hides the Actor implementation of a data type behind a regular interface
  *
  * @author Ronny Bräunlich
  */
trait FormicDataType {

  var connection: ActorRef = _

  val dataTypeName: DataTypeName

  var callback: () => Unit

  var actor: ActorRef = _
}
