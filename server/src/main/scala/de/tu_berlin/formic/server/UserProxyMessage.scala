package de.tu_berlin.formic.server

import akka.actor.ActorRef
import de.tu_berlin.formic.common.message.OperationMessage

/**
  * @author Ronny Bräunlich
  */
sealed trait UserProxyMessage

case class Connected(outgoing: ActorRef) extends UserProxyMessage
