package de.tu_berlin.formic.client

import akka.actor.ActorRef

/**
  * @author Ronny Bräunlich
  */
trait WebSocketFactory {

  def createConnection(url: String, connection: ActorRef): WebSocketWrapper

}
