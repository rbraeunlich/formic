package de.tu_berlin.formic.client

import akka.actor.{Actor, ActorLogging, ActorRef}
import de.tu_berlin.formic.client.Dispatcher._
import de.tu_berlin.formic.common.datastructure.client.AbstractClientDataStructureFactory.NewDataStructureCreated
import de.tu_berlin.formic.common.DataStructureInstanceId
import de.tu_berlin.formic.common.message._

/**
  * @author Ronny Bräunlich
  */
class Dispatcher(val outgoingConnection: ActorRef, val newInstanceCallback: ActorRef, val instantiator: ActorRef) extends Actor with ActorLogging {

  var instances: Map[DataStructureInstanceId, ActorRef] = Map.empty

  def receive = {
    case op: OperationMessage =>
      instances.find(t => t._1 == op.dataStructureInstanceId) match {
        case Some((k, v)) => v ! op
        case None => log.warning(s"Did not find data structure instance with id ${op.dataStructureInstanceId}, dropping message $op")
      }

    case rep: UpdateResponse =>
      instances.find(t => t._1 == rep.dataStructureInstanceId) match {
        case Some(_) => log.debug(s"Ignoring $rep because data structure already exists")
        case None => instantiator ! WrappedUpdateResponse(outgoingConnection, rep)
      }

    case created: NewDataStructureCreated =>
      instances += (created.dataStructureInstanceId -> created.dataStructureActor)
      newInstanceCallback ! created

    case ErrorMessage(errorText) => log.error("Error from WebSocket connection: " + errorText)

    case rep: CreateResponse =>
      log.debug(s"Dispatcher received CreateResponse $rep")
      instances.find(t => t._1 == rep.dataStructureInstanceId) match {
        case Some((k, v)) => v ! rep
        case None => log.warning(s"Did not find data structure instance with id ${rep.dataStructureInstanceId}, dropping message $rep")
      }

    case (ref:ActorRef, req:CreateRequest) =>
      //this is a little hack to inform the Dispatcher about new, locally created data types
      instances += (req.dataStructureInstanceId -> ref)

    case hist: HistoricOperationRequest => outgoingConnection ! hist

    case RequestKnownDataStructureIds => sender ! KnownDataStructureIds(instances.keySet)
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
  case object RequestKnownDataStructureIds

  /**
    * Answer to the RequestKnownDataTypeId message
    */
  case class KnownDataStructureIds(ids: Set[DataStructureInstanceId])
}
