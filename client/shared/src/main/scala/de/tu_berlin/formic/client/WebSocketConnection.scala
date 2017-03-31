package de.tu_berlin.formic.client

import akka.actor.{Actor, ActorLogging, ActorRef, Props, _}
import akka.pattern.ask
import akka.util.Timeout
import akka.util.Timeout._
import de.tu_berlin.formic.client.Dispatcher._
import de.tu_berlin.formic.client.WebSocketConnection._
import de.tu_berlin.formic.common.ClientId
import de.tu_berlin.formic.common.json.FormicJsonProtocol._
import de.tu_berlin.formic.common.message._
import upickle.default._
import de.tu_berlin.formic.client.collection.FiniteQueue._
import de.tu_berlin.formic.common.json.FormicJsonProtocol

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.util.{Failure, Success}

/**
  * @author Ronny Bräunlich
  */
class WebSocketConnection(val newInstanceCallback: ActorRef,
                          val instantiator: ActorRef,
                          val clientId: ClientId,
                          val webSocketConnectionFactory: WebSocketFactory,
                          val url: String,
                          val bufferSize: Int,
                          val jsonProtocol: FormicJsonProtocol)
                         (implicit val ec: ExecutionContext)
  extends Actor
    with Connection
    with OutgoingConnection
    with ActorLogging {

  var dispatcher: ActorRef = _
  var connectionTry: Cancellable = _
  var webSocketConnection: WebSocketWrapper = _

  implicit val writer = jsonProtocol.writer
  implicit val reader = jsonProtocol.reader

  def retryConnection(): Unit = {
    webSocketConnectionFactory.createConnection(url, self)
    log.debug(s"New WebSocket connection created for url: $url")
  }

  override def preStart(): Unit = {
    super.preStart()
    dispatcher = context.actorOf(Props(new Dispatcher(self, newInstanceCallback, instantiator)), "dispatcher")
    connectionTry = context.system.scheduler.schedule(100.millis, 5.seconds) {
      retryConnection()
    }
    self ! Start
  }

  override def postStop(): Unit = {
    super.postStop()
    if (connectionTry != null) connectionTry.cancel()
  }


  def receive = {
    case Start =>
      log.debug("Starting")
      context.become(offline(scala.collection.mutable.Queue.empty))
  }

  def online: Receive = {
    case OnError(errorMessage) =>
      log.debug(s"User $clientId received OnError message")
      dispatcher ! ErrorMessage(errorMessage)
    case OnMessage(msg) =>
      log.debug(s"User $clientId received WebSocket message: $msg")
      dispatcher ! read[FormicMessage](msg)
    case OnClose(code) =>
      log.warning(s"User $clientId became offline with code " + code)
      connectionTry = context.system.scheduler.schedule(100.millis, 5.seconds) {
        retryConnection()
      }
      context.become(offline(scala.collection.mutable.Queue.empty))
    case (ref: ActorRef, req: CreateRequest) =>
      log.debug(s"User $clientId received CreateRequest: $req")
      //this is a little hack because the FormicSystem does not know the dispatcher
      //create requests can only be from the local client because remote ones arrive as FormicMsgs
      dispatcher ! (ref, req)
      sendMessageViaWebSocket(req)
    case hist: HistoricOperationRequest =>
      sendMessageViaWebSocket(hist)
    case upd: UpdateRequest =>
      sendMessageViaWebSocket(upd)
    case op: OperationMessage =>
      sendMessageViaWebSocket(op)
  }

  def offline(buffer: scala.collection.mutable.Queue[FormicMessage]): Receive = {
    case OnConnect(ws: WebSocketWrapper) =>
      log.debug("Connecting")
      connectionTry.cancel
      webSocketConnection = ws
      implicit val timeout: Timeout = 3.seconds
      val knownDataTypeIdsFuture = dispatcher ? RequestKnownDataStructureIds
      knownDataTypeIdsFuture.onComplete{
        case Success(msg) =>
          val ids = msg.asInstanceOf[KnownDataStructureIds].ids
          ids.filterNot(id => buffer.exists(msg => msg.isInstanceOf[CreateRequest] && msg.asInstanceOf[CreateRequest].dataStructureInstanceId == id))
            .foreach{
            id => sendMessageViaWebSocket(UpdateRequest(clientId, id))
          }
          buffer.foreach(msg => sendMessageViaWebSocket(msg))
        case Failure(ex) => log.error(ex, "Error while asking dispatcher about data type ids")
      }
      context.become(online)
    case OnError(errorMessage) =>
      log.debug(s"User $clientId received OnError message")
      dispatcher ! ErrorMessage(errorMessage)
    case OnClose(code) =>
      log.warning(s"User $clientId staying offline with code $code")
    case (ref: ActorRef, req: CreateRequest) =>
      log.debug(s"User $clientId buffering $req")
      //this is a little hack because the FormicSystem does not know the dispatcher
      //create requests can only be from the local client because remote ones arrive as FormicMsgs
      dispatcher ! (ref, req)
      buffer.enqueueFinite(req, bufferSize)
    case hist: HistoricOperationRequest =>
      log.debug(s"User $clientId buffering $hist")
      buffer.enqueueFinite(hist, bufferSize)
    case upd: UpdateRequest =>
      log.debug(s"User $clientId buffering $upd")
      buffer.enqueueFinite(upd, bufferSize)
    case op: OperationMessage =>
      log.debug(s"User $clientId buffering $op")
      buffer.enqueueFinite(op, bufferSize)
  }

  /**
    *
    * Uses the webSocketConnection to send the specific message.
    *
    * @param msg the message to send to the server
    */
  def sendMessageViaWebSocket(msg: FormicMessage) = {
    msg match {
      case req: CreateRequest =>
        webSocketConnection.send(write(CreateRequest(clientId, req.dataStructureInstanceId, req.dataStructure)))
      case hist: HistoricOperationRequest =>
        log.debug(s"User $clientId sending $hist")
        webSocketConnection.send(write(HistoricOperationRequest(clientId, hist.dataStructureInstanceId, hist.sinceId)))
      case upd: UpdateRequest =>
        log.debug(s"User $clientId sending $upd")
        webSocketConnection.send(write(UpdateRequest(clientId, upd.dataStructureInstanceId)))
      case op: OperationMessage =>
        log.debug(s"User $clientId sending $op")
        webSocketConnection.send(write(OperationMessage(clientId, op.dataStructureInstanceId, op.dataStructure, op.operations)))
      case other => throw new IllegalArgumentException(s"Client should not send this type of message: $other")
    }
  }
}

object WebSocketConnection {

  sealed trait WebSocketConnectionMessage

  case class OnError(message: String) extends WebSocketConnectionMessage

  case class OnMessage(message: String) extends WebSocketConnectionMessage

  case class OnClose(closeCode: Int) extends WebSocketConnectionMessage

  case class OnConnect(webSocketWrapper: WebSocketWrapper) extends WebSocketConnectionMessage

  /**
    * An initial message to the WebSocketConnection so that it can start being offline and has the
    * cancellable.
    */
  case object Start

}
