package de.tu_berlin.formic.server

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import de.tu_berlin.formic.common.server.datastructure.AbstractServerDataStructure.HistoricOperationsAnswer
import de.tu_berlin.formic.common.datastructure.DataStructureName
import de.tu_berlin.formic.common.message._
import de.tu_berlin.formic.common.server.datastructure.NewDataStructureCreated
import de.tu_berlin.formic.common.{ClientId, DataStructureInstanceId}
import de.tu_berlin.formic.server.UserProxy.NewDataStructureSubscription

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.{Failure, Success}

/**
  * @author Ronny Bräunlich
  */
class UserProxy(val factories: Map[DataStructureName, ActorRef], val id: ClientId = ClientId()) extends Actor with ActorLogging{

  var watchlist: Map[DataStructureInstanceId, ActorRef] = Map.empty

  import context._

  def receive = {
    case Connected(outgoing) =>
      log.debug(s"User $id connected")
      val subscriber = context.actorOf(Props(new OperationMessageSubscriber(outgoing, id)))
      context.system.eventStream.subscribe(subscriber, classOf[OperationMessage])
      context.become(connected(outgoing, subscriber))
  }

  def connected(outgoing: ActorRef, subscriber: ActorRef): Receive = {

    case req: CreateRequest =>
      log.debug(s"Incoming CreateRequest from user $id: $req")
      val factory = factories.find(t => t._1 == req.dataStructure)
      factory match {
        case Some(f) => f._2 ! req
        case None => throw new IllegalArgumentException("Unknown data structure")
      }

    case NewDataStructureCreated(dataStructureInstanceId, ref) =>
      watchlist += (dataStructureInstanceId -> ref)
      subscriber ! NewDataStructureSubscription(dataStructureInstanceId, ref)
      log.debug(s"Sending CreateResponse for $dataStructureInstanceId")
      outgoing ! CreateResponse(dataStructureInstanceId)

    case hist: HistoricOperationRequest =>
      log.debug(s"Incoming HistoricOperationRequest from user $id: $hist")
      val dataStructureInstance = watchlist.find(t => t._1 == hist.dataStructureInstanceId)
      dataStructureInstance match {
        case Some((_, ref)) => ref ! hist
        case None => throw new IllegalArgumentException(s"Data structure instance with id $dataStructureInstance unkown")
      }

    case req: UpdateRequest =>
      log.debug(s"Incoming UpdateRequest from user $id: $req")
      val actorSelection = context.actorSelection(s"../*/${req.dataStructureInstanceId.id}").resolveOne(3 seconds)
      //blocking here is necessary to ensure no messages for a data type are received while we look it up
      Await.ready(actorSelection, 6.seconds)
      //onComplete might be executed async, but we need to be sync here
      actorSelection.value.get match {
        case Success(ref) =>
          watchlist += (req.dataStructureInstanceId -> ref)
          ref ! req
        case Failure(ex) => throw new IllegalArgumentException(s"Data structure instance with id ${req.dataStructureInstanceId.id} not found. Exception: $ex")
      }

    case rep: UpdateResponse =>
      log.debug(s"Sending UpdateResponse to user $id: $rep")
      subscriber ! NewDataStructureSubscription(rep.dataStructureInstanceId, sender)
      outgoing ! rep

    case operationMessage: OperationMessage =>
      log.debug(s"Incoming operation from user $id: $operationMessage")
      val dataStructureInstance = watchlist.find(t => t._1 == operationMessage.dataStructureInstanceId)
      dataStructureInstance match {
        case Some((_, ref)) => ref ! operationMessage
        case None => throw new IllegalArgumentException(s"Data structure instance with id ${operationMessage.dataStructureInstanceId.id} unkown")
      }

    case HistoricOperationsAnswer(opMsg) =>
      log.debug(s"Sending historic operations to user $id: $opMsg")
      outgoing ! opMsg
  }
}

class OperationMessageSubscriber(val outgoingConnection: ActorRef, val clientId: ClientId) extends Actor with ActorLogging{

  var watchlist: Map[DataStructureInstanceId, ActorRef] = Map.empty

  def receive = {
    case op: OperationMessage =>
      val dataStructureInstance = watchlist.find(t => t._1 == op.dataStructureInstanceId)
      dataStructureInstance match {
        case Some(_) =>
          log.debug(s"Sending operation message $op to user $clientId")
          outgoingConnection ! op
        case None => //Client is not interested in the data type that changed
      }

    case NewDataStructureSubscription(dataStructureInstanceId, actorRef) =>
      watchlist += (dataStructureInstanceId -> actorRef)
  }
}

object UserProxy {

  case class NewDataStructureSubscription(dataStructureInstanceId: DataStructureInstanceId, actorRef: ActorRef)

}