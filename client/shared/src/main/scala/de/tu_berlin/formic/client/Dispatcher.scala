package de.tu_berlin.formic.client

import akka.actor.{Actor, ActorLogging, ActorRef}
import de.tu_berlin.formic.client.Dispatcher.{ConnectionEstablished, ErrorMessage}
import de.tu_berlin.formic.common.DataTypeInstanceId
import de.tu_berlin.formic.common.message.{OperationMessage, UpdateResponse}
import de.tu_berlin.formic.common.server.datatype.NewDataTypeCreated

/**
  * @author Ronny Bräunlich
  */
class Dispatcher(val outgoingConnection: OutgoingConnection, val newInstanceCallback: ActorRef, val instantiator: ActorRef) extends Actor with ActorLogging {

  var instances: Map[DataTypeInstanceId, ActorRef] = Map.empty

  def receive = {
    case op: OperationMessage =>
      instances.find(t => t._1 == op.dataTypeInstanceId) match {
        case Some((k, v)) => v ! op
        case None => log.warning(s"Did not find data type instance with id ${op.dataTypeInstanceId}, dropping message $op")
      }
    case rep: UpdateResponse =>
      instantiator ! rep
    case created: NewDataTypeCreated =>
      instances += (created.dataTypeInstanceId -> created.ref)
    case ConnectionEstablished => //TODO
    case ErrorMessage(errorText) => log.error("Error from WebSocket connection: " + errorText)
    //TODO more?
  }
}

object Dispatcher {

  case object ConnectionEstablished

  case class ErrorMessage(errorText: String)

}
