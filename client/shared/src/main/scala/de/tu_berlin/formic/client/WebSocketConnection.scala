package de.tu_berlin.formic.client

import akka.actor.{Actor, ActorLogging, ActorRef, Props, _}
import de.tu_berlin.formic.client.Dispatcher._
import de.tu_berlin.formic.client.WebSocketConnection._
import de.tu_berlin.formic.common.ClientId
import de.tu_berlin.formic.common.json.FormicJsonProtocol._
import de.tu_berlin.formic.common.message._
import upickle.default._

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

/**
  * @author Ronny Bräunlich
  */
class WebSocketConnection(val newInstanceCallback: ActorRef,
                          val instantiator: ActorRef,
                          val clientId: ClientId,
                          val webSocketConnectionFactory: WebSocketFactory,
                          val url: String)
                         (implicit val ec: ExecutionContext)
  extends Actor
    with Connection
    with OutgoingConnection
    with ActorLogging {

  var dispatcher: ActorRef = _
  var connectionTry: Cancellable = _
  var webSocketConnection: WebSocketWrapper = _

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
      context.become(offline(scala.collection.mutable.Set.empty))
  }

  def online: Receive = {
    case OnError(errorMessage) =>
      log.debug(s"Received OnError message")
      dispatcher ! ErrorMessage(errorMessage)
    case OnMessage(msg) =>
      log.debug(s"Received WebSocket message: $msg")
      dispatcher ! read[FormicMessage](msg)
    case OnClose(code) =>
      log.warning("Became offline with code " + code)
      connectionTry = context.system.scheduler.schedule(100.millis, 5.seconds) {
        retryConnection()
      }
      context.become(offline(scala.collection.mutable.Set.empty))
    case (ref: ActorRef, req: CreateRequest) =>
      log.debug(s"Received CreateRequest: $req")
      //this is a little hack because the FormicSystem does not know the dispatcher
      //create requests can only be from the local client because remote ones arrive as FormicMsgs
      dispatcher ! (ref, req)
      sendMessageViaWebSocket(req)
    case hist: HistoricOperationRequest =>
      log.debug(s"Sending $hist")
      sendMessageViaWebSocket(hist)
    case upd: UpdateRequest =>
      log.debug(s"Sending $upd")
      sendMessageViaWebSocket(upd)
    case op: OperationMessage =>
      log.debug(s"Sending $op")
      sendMessageViaWebSocket(op)
  }

  def offline(buffer: scala.collection.mutable.Set[FormicMessage]): Receive = {
    case OnConnect(ws: WebSocketWrapper) =>
      log.debug("Connecting")
      connectionTry.cancel
      webSocketConnection = ws
      buffer.foreach(msg => sendMessageViaWebSocket(msg))
      context.become(online)
    case OnError(errorMessage) =>
      log.debug(s"Received OnError message")
      dispatcher ! ErrorMessage(errorMessage)
    case OnClose(code) =>
      log.warning("Staying offline with code " + code)
    case (ref: ActorRef, req: CreateRequest) =>
      log.debug(s"Buffering $req")
      //this is a little hack because the FormicSystem does not know the dispatcher
      //create requests can only be from the local client because remote ones arrive as FormicMsgs
      dispatcher ! (ref, req)
      buffer += req
    case hist: HistoricOperationRequest =>
      log.debug(s"Buffering $hist")
      buffer += hist
    case upd: UpdateRequest =>
      log.debug(s"Buffering $upd")
      buffer += upd
    case op: OperationMessage =>
      log.debug(s"Buffering $op")
      buffer += op
  }

  /**
    *
    * Uses the webSocketConnection to send the specific message after inserting the clientId into
    * a copy.
    *
    * @param msg the message to send to the server
    */
  def sendMessageViaWebSocket(msg: FormicMessage) = {
    msg match {
      case req: CreateRequest =>
        webSocketConnection.send(write(CreateRequest(clientId, req.dataTypeInstanceId, req.dataType)))
      case hist: HistoricOperationRequest =>
        webSocketConnection.send(write(HistoricOperationRequest(clientId, hist.dataTypeInstanceId, hist.sinceId)))
      case upd: UpdateRequest =>
        webSocketConnection.send(write(UpdateRequest(clientId, upd.dataTypeInstanceId)))
      case op: OperationMessage =>
        val operations = op.operations
        operations.foreach(operation => operation.clientId = clientId)
        webSocketConnection.send(write(OperationMessage(clientId, op.dataTypeInstanceId, op.dataType, operations)))
      case other => throw new IllegalArgumentException(s"Client should not send this type of message: $other")
    }
  }
}

object WebSocketConnection {

  case class OnError(message: String)

  case class OnMessage(message: String)

  case class OnClose(closeCode: Int)

  case class OnConnect(webSocketWrapper: WebSocketWrapper)

  /**
    * An initial message to the WebSocketConnection so that it can start being offline and has the
    * cancellable.
    */
  case object Start

}
