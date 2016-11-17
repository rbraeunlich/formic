package de.tu_berlin.formic.server

import akka.actor.{Actor, ActorRef}
import de.tu_berlin.formic.common.message.CreateResponse
import de.tu_berlin.formic.server.datatype.NewDataTypeCreated

/**
  * @author Ronny Bräunlich
  */
class UserProxy extends Actor {

  def receive = {
    case Connected(outgoing) =>
      context.become(connected(outgoing))
  }

  def connected(outgoing: ActorRef): Receive = {

    case IncomingMessage(text) =>


    case NewDataTypeCreated(id, ref) =>
      outgoing ! CreateResponse(id)


  }
}
