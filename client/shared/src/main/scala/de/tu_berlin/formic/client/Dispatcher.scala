package de.tu_berlin.formic.client

import akka.actor.{Actor, ActorLogging, ActorRef}
import de.tu_berlin.formic.client.Dispatcher.{ErrorMessage, WrappedUpdateResponse}
import de.tu_berlin.formic.client.datatype.AbstractClientDataTypeFactory.NewDataTypeCreated
import de.tu_berlin.formic.common.DataTypeInstanceId
import de.tu_berlin.formic.common.message.{CreateResponse, OperationMessage, UpdateResponse}

/**
  * @author Ronny Bräunlich
  */
class Dispatcher(val outgoingConnection: ActorRef, val newInstanceCallback: ActorRef, val instantiator: ActorRef) extends Actor with ActorLogging {

  var instances: Map[DataTypeInstanceId, ActorRef] = Map.empty

  def receive = {
    case op: OperationMessage =>
      instances.find(t => t._1 == op.dataTypeInstanceId) match {
        case Some((k, v)) => v ! op
        case None => log.warning(s"Did not find data type instance with id ${op.dataTypeInstanceId}, dropping message $op")
      }
    case rep: UpdateResponse =>
      instantiator ! WrappedUpdateResponse(outgoingConnection, rep)
    case created: NewDataTypeCreated =>
      instances += (created.dataTypeInstanceId -> created.dataTypeActor)
      newInstanceCallback ! created
    case ErrorMessage(errorText) => log.error("Error from WebSocket connection: " + errorText)
    case rep: CreateResponse =>
      //TODO the data type may now send its operations to the server
    //TODO more?
  }
}

object Dispatcher {

  case class ErrorMessage(errorText: String)

  case class WrappedUpdateResponse(outgoingConnection: ActorRef, updateResponse: UpdateResponse)

}
