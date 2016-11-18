package de.tu_berlin.formic.server

import akka.actor.ActorRef

/**
  * @author Ronny Bräunlich
  */
sealed trait UserProxyMessage

case class IncomingMessage(text: String) extends UserProxyMessage

case class OutgoingMessage(text: String) extends UserProxyMessage

case class Connected(outgoing: ActorRef) extends UserProxyMessage
