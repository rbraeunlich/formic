package de.tu_berlin.formic.common.datastructure.client

import akka.actor.Actor
import de.tu_berlin.formic.common.datastructure.client.CallbackWrapper.Invoke

/**
  * @author Ronny Bräunlich
  */
class CallbackWrapper(callback: (ClientDataStructureEvent) => Unit) extends Actor {

  def receive = {
    case Invoke(event) => callback(event)
  }

}

object CallbackWrapper {
  case class Invoke(event: ClientDataStructureEvent)
}
