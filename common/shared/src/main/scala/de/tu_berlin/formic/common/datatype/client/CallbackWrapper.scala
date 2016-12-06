package de.tu_berlin.formic.common.datatype.client

import akka.actor.Actor

/**
  * @author Ronny Bräunlich
  */
class CallbackWrapper(callback: () => Unit) extends Actor {

  def receive = {
    case _ => callback()
  }

}

object CallbackWrapper {
  case object Invoke
}
