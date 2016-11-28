package de.tu_berlin.formic.client

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import de.tu_berlin.formic.client.Dispatcher._
import de.tu_berlin.formic.client.WebSocketConnection.{OnConnect, OnError, OnMessage}
import de.tu_berlin.formic.common.json.FormicJsonProtocol._
import de.tu_berlin.formic.common.message.FormicMessage
import org.scalajs.dom._
import upickle.default._

/**
  * @author Ronny Bräunlich
  */
class WebSocketConnection(val newInstanceCallback: ActorRef, val instantiator: ActorRef)(implicit val actorSystem: ActorSystem) extends Actor with Connection with OutgoingConnection {

  //TODO read from config
  val url = "0.0.0.0:8080"

  private var dispatcher: ActorRef = _

  val webSocketConnection = new WebSocket(url)
  webSocketConnection.onopen = { event: Event => self ! OnConnect }
  webSocketConnection.onerror = { event: ErrorEvent => self ! OnError(event.message) }
  webSocketConnection.onmessage = { event: MessageEvent => self ! OnMessage(event.data.toString) }

  def receive = {
    case OnConnect => dispatcher = actorSystem.actorOf(Props(new Dispatcher(self, newInstanceCallback, instantiator)))
    case OnError(errorMessage) => dispatcher ! ErrorMessage(errorMessage)
    case OnMessage(msg) => dispatcher ! read[FormicMessage](msg)
    case formicMessage: FormicMessage => webSocketConnection.send(write(formicMessage))
  }
}

object WebSocketConnection {

  case object OnConnect

  case class OnError(message: String)

  case class OnMessage(message: String)

}
