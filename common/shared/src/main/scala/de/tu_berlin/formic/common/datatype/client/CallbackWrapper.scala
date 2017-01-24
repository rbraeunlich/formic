package de.tu_berlin.formic.common.datatype.client

import akka.actor.Actor
import de.tu_berlin.formic.common.datatype.client.CallbackWrapper.Invoke

/**
  * @author Ronny Bräunlich
  */
class CallbackWrapper(callback: (ClientDataTypeEvent) => Unit) extends Actor {

  def receive = {
    case Invoke(event) => callback(event)
  }

}

object CallbackWrapper {
  case class Invoke(event: ClientDataTypeEvent)
}
