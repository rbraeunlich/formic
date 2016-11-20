package de.tu_berlin.formic.server

import akka.actor.{Actor, ActorRef}
import de.tu_berlin.formic.common.datatype.DataTypeName
import de.tu_berlin.formic.common.json.FormicJsonProtocol._
import de.tu_berlin.formic.common.message._
import de.tu_berlin.formic.common.{ClientId, DataTypeInstanceId}
import de.tu_berlin.formic.server.datatype.NewDataTypeCreated
import upickle.default._

import scala.concurrent.duration._
import scala.util.{Failure, Success}
/**
  * @author Ronny Bräunlich
  */
class UserProxy(val factories: Map[DataTypeName, ActorRef]) extends Actor {
  import context._
  val id = ClientId()

  var watchlist: Map[DataTypeInstanceId, ActorRef] = Map.empty

  def receive = {
    case Connected(outgoing) =>
      context.system.eventStream.subscribe(self, classOf[OperationMessage])
      context.become(connected(outgoing))
  }

  def connected(outgoing: ActorRef): Receive = {

    case IncomingMessage(text) =>
      val message = read[FormicMessage](text)
      message match {
        case op: OperationMessage => sendOperationMessageToDataTypeInstance(op)
        case _ => self ! message
      }

    case req: CreateRequest =>
      val factory = factories.find(t => t._1 == req.dataType)
      factory match {
        case Some(f) => f._2 ! req
        case None => //TODO ErrorMessage to client
      }

    case NewDataTypeCreated(dataTypeInstanceId, ref) =>
      watchlist += (dataTypeInstanceId -> ref)
      outgoing ! OutgoingMessage(write(CreateResponse(dataTypeInstanceId)))

    case op: OperationMessage =>
      val dataTypeInstance = watchlist.find(t => t._1 == op.dataTypeInstanceId)
      dataTypeInstance match {
        case Some(_) => outgoing ! OutgoingMessage(write(op))
        case None => //Client is not interested in the data type that changed
      }

    case hist: HistoricOperationRequest =>
      val dataTypeInstance = watchlist.find(t => t._1 == hist.dataTypeInstanceId)
      dataTypeInstance match {
        case Some((_, ref)) => ref ! hist
        case None => //TODO Error
      }

    case req: UpdateRequest =>
      context.actorSelection(s"../*/${req.dataTypeInstanceId.id}").resolveOne(3 seconds).onComplete {
        case Success(ref) =>  ref ! req
        case Failure(ex) => //TODO error
      }

    case rep: UpdateResponse => outgoing ! OutgoingMessage(write(rep))
  }

  /**
    * In order to distinguish OperationMessages the Client sent and the ones incoming from the subscription
    * to the EventBus, this message is needed to directly send the remote OperationMessages to the data
    * type instances
    */
  def sendOperationMessageToDataTypeInstance(op: OperationMessage): Unit = {
    val dataTypeInstance = watchlist.find(t => t._1 == op.dataTypeInstanceId)
    dataTypeInstance match {
      case Some((_, ref)) => ref ! op
      case None => //TODO ErrorMessage to client
    }
  }
}