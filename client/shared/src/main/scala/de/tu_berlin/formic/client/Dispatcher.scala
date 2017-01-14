package de.tu_berlin.formic.client

import akka.actor.{Actor, ActorLogging, ActorRef}
import de.tu_berlin.formic.client.Dispatcher._
import de.tu_berlin.formic.common.datatype.client.AbstractClientDataTypeFactory.NewDataTypeCreated
import de.tu_berlin.formic.common.DataTypeInstanceId
import de.tu_berlin.formic.common.message._

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
      instances.find(t => t._1 == rep.dataTypeInstanceId) match {
        case Some(_) => log.debug(s"Ignoring $rep because data type already exists")
        case None => instantiator ! WrappedUpdateResponse(outgoingConnection, rep)
      }

    case created: NewDataTypeCreated =>
      instances += (created.dataTypeInstanceId -> created.dataTypeActor)
      newInstanceCallback ! created

    case ErrorMessage(errorText) => log.error("Error from WebSocket connection: " + errorText)

    case rep: CreateResponse =>
      log.debug(s"Dispatcher received CreateResponse $rep")
      instances.find(t => t._1 == rep.dataTypeInstanceId) match {
        case Some((k, v)) => v ! rep
        case None => log.warning(s"Did not find data type instance with id ${rep.dataTypeInstanceId}, dropping message $rep")
      }

    case (ref:ActorRef, req:CreateRequest) =>
      //this is a little hack to inform the Dispatcher about new, locally created data types
      instances += (req.dataTypeInstanceId -> ref)

    case hist: HistoricOperationRequest => outgoingConnection ! hist

    case RequestKnownDataTypeIds => sender ! KnownDataTypeIds(instances.keySet)
  }
}

object Dispatcher {

  case class ErrorMessage(errorText: String)

  /**
    * To be able to pass the outgoing connection to the next actor, the UpdateResponse has to be wrapped.
    */
  case class WrappedUpdateResponse(outgoingConnection: ActorRef, updateResponse: UpdateResponse)

  /**
    * Message to enable the WebSocketConnection to ask the Dispatcher for all the data types that exist on the client.
    */
  case object RequestKnownDataTypeIds

  /**
    * Answer to the RequestKnownDataTypeId message
    */
  case class KnownDataTypeIds(ids: Set[DataTypeInstanceId])
}
