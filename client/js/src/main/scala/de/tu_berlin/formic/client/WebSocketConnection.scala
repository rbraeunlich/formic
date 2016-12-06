package de.tu_berlin.formic.client

import akka.actor.{Actor, ActorLogging, ActorRef, Props, _}
import de.tu_berlin.formic.client.Dispatcher._
import de.tu_berlin.formic.client.WebSocketConnection.{OnConnect, OnError, OnMessage}
import de.tu_berlin.formic.common.ClientId
import de.tu_berlin.formic.common.json.FormicJsonProtocol._
import de.tu_berlin.formic.common.message._
import org.scalajs.dom
import upickle.default._
import org.scalajs.dom._

/**
  * @author Ronny Bräunlich
  */
class WebSocketConnection(val newInstanceCallback: ActorRef,
                          val instantiator: ActorRef,
                          val clientId: ClientId,
                          val webSocketConnectionFactory: WebSocketFactory,
                          val url: String)
  extends Actor
    with Connection
    with OutgoingConnection
    with ActorLogging {

  var dispatcher: ActorRef = _
  var webSocketConnection: dom.WebSocket = webSocketConnectionFactory.createConnection(url)
  webSocketConnection.onopen = { event: Event => self ! OnConnect }

  def receive = {
    case OnConnect =>
      log.debug(s"Received OnConnect message")
      dispatcher = context.actorOf(Props(new Dispatcher(self, newInstanceCallback, instantiator)), "dispatcher")
      webSocketConnection.onerror = { event: ErrorEvent => self ! OnError(event.message) }
      webSocketConnection.onmessage = { event: MessageEvent => self ! OnMessage(event.data.toString) }
    case OnError(errorMessage) =>
      log.debug(s"Received OnError message")
      dispatcher ! ErrorMessage(errorMessage)
    case OnMessage(msg) =>
      log.debug(s"Received WebSocket message: $msg")
      dispatcher ! read[FormicMessage](msg)
    //TODO Buffer messages when being offline
    //gotta add the client id
    case (ref: ActorRef, req: CreateRequest) =>
      log.debug(s"Received CreateRequest: $req")
      //this is a little hack because the FormicSystem does not know the dispatcher
      //create requests can only be from the local client because remote ones arrive as FormicMsgs
      dispatcher ! (ref, req)
      webSocketConnection.send(write(CreateRequest(clientId, req.dataTypeInstanceId, req.dataType)))
    case hist: HistoricOperationRequest =>
      log.debug(s"Sending $hist")
      webSocketConnection.send(write(HistoricOperationRequest(clientId, hist.dataTypeInstanceId, hist.sinceId)))
    case upd: UpdateRequest =>
      log.debug(s"Sending $upd")
      webSocketConnection.send(write(UpdateRequest(clientId, upd.dataTypeInstanceId)))
    case op: OperationMessage =>
      log.debug(s"Sending $op")
      val operations = op.operations
      operations.foreach(operation => operation.clientId = clientId)
      webSocketConnection.send(write(OperationMessage(clientId, op.dataTypeInstanceId, op.dataType, operations)))
  }
}

object WebSocketConnection {

  case object OnConnect

  case class OnError(message: String)

  case class OnMessage(message: String)

}
