package de.tu_berlin.formic.server

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import de.tu_berlin.formic.common.server.datatype.AbstractServerDataType.HistoricOperationsAnswer
import de.tu_berlin.formic.common.datatype.DataTypeName
import de.tu_berlin.formic.common.message._
import de.tu_berlin.formic.common.server.datatype.NewDataTypeCreated
import de.tu_berlin.formic.common.{ClientId, DataTypeInstanceId}
import de.tu_berlin.formic.server.UserProxy.NewDataTypeSubscription

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.{Failure, Success}

/**
  * @author Ronny Bräunlich
  */
class UserProxy(val factories: Map[DataTypeName, ActorRef], val id: ClientId = ClientId()) extends Actor with ActorLogging{

  var watchlist: Map[DataTypeInstanceId, ActorRef] = Map.empty

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
      val factory = factories.find(t => t._1 == req.dataType)
      factory match {
        case Some(f) => f._2 ! req
        case None => throw new IllegalArgumentException("Unknown data type")
      }

    case NewDataTypeCreated(dataTypeInstanceId, ref) =>
      watchlist += (dataTypeInstanceId -> ref)
      subscriber ! NewDataTypeSubscription(dataTypeInstanceId, ref)
      log.debug(s"Sending CreateResponse for $dataTypeInstanceId")
      outgoing ! CreateResponse(dataTypeInstanceId)

    case hist: HistoricOperationRequest =>
      log.debug(s"Incoming HistoricOperationRequest from user $id: $hist")
      val dataTypeInstance = watchlist.find(t => t._1 == hist.dataTypeInstanceId)
      dataTypeInstance match {
        case Some((_, ref)) => ref ! hist
        case None => throw new IllegalArgumentException(s"Data type instance with id $dataTypeInstance unkown")
      }

    case req: UpdateRequest =>
      log.debug(s"Incoming UpdateRequest from user $id: $req")
      val actorSelection = context.actorSelection(s"../*/${req.dataTypeInstanceId.id}").resolveOne(3 seconds)
      actorSelection.onComplete {
        case Success(ref) =>
          watchlist += (req.dataTypeInstanceId -> ref)
          ref ! req
        case Failure(ex) => throw new IllegalArgumentException(s"Data type instance with id ${req.dataTypeInstanceId.id} not found. Exception: $ex")
      }
      //blocking here is necessary to ensure no messages for a data type are received while we look it up
      Await.ready(actorSelection, 3.seconds)

    case rep: UpdateResponse =>
      log.debug(s"Sending UpdateResponse to user $id: $rep")
      subscriber ! NewDataTypeSubscription(rep.dataTypeInstanceId, sender)
      outgoing ! rep

    case operationMessage: OperationMessage =>
      log.debug(s"Incoming operation from user $id: $operationMessage")
      val dataTypeInstance = watchlist.find(t => t._1 == operationMessage.dataTypeInstanceId)
      dataTypeInstance match {
        case Some((_, ref)) => ref ! operationMessage
        case None => throw new IllegalArgumentException(s"Data type instance with id ${operationMessage.dataTypeInstanceId.id} unkown")
      }

    case HistoricOperationsAnswer(opMsg) =>
      log.debug(s"Sending historic operations to user $id: $opMsg")
      outgoing ! opMsg
  }
}

class OperationMessageSubscriber(val outgoingConnection: ActorRef, val clientId: ClientId) extends Actor with ActorLogging{

  var watchlist: Map[DataTypeInstanceId, ActorRef] = Map.empty

  def receive = {
    case op: OperationMessage =>
      val dataTypeInstance = watchlist.find(t => t._1 == op.dataTypeInstanceId)
      dataTypeInstance match {
        case Some(_) =>
          log.debug(s"Sending operation message $op to user $clientId")
          outgoingConnection ! op
        case None => //Client is not interested in the data type that changed
      }

    case NewDataTypeSubscription(dataTypeInstanceId, actorRef) =>
      watchlist += (dataTypeInstanceId -> actorRef)
  }
}

object UserProxy {

  case class NewDataTypeSubscription(dataTypeInstanceId: DataTypeInstanceId, actorRef: ActorRef)

}