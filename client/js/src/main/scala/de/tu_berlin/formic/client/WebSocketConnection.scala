package de.tu_berlin.formic.client

import akka.actor.{ActorRef, ActorSystem, Props}
import de.tu_berlin.formic.common.message.FormicMessage
import org.scalajs.dom._
import de.tu_berlin.formic.common.json.FormicJsonProtocol._
import upickle.default._
import de.tu_berlin.formic.client.Dispatcher._
/**
  * @author Ronny Bräunlich
  */
class WebSocketConnection(val newInstanceCallback: ActorRef, val instantiator: ActorRef)(implicit val actorSystem: ActorSystem) extends Connection with OutgoingConnection{

  //TODO read from config
  val url = "0.0.0.0:8080"

  private var dispatcher: ActorRef = _

  val webSocketConnection = new WebSocket(url)
  webSocketConnection.onopen = {event: Event => onConnect()}
  webSocketConnection.onerror = {event: ErrorEvent => onError(event.message)}
  webSocketConnection.onmessage = {event: MessageEvent => onMessage(read[FormicMessage](event.data.toString))}

  override def onConnect(): Unit = {
    dispatcher = actorSystem.actorOf(Props(new Dispatcher(this, newInstanceCallback, instantiator)))
    dispatcher ! ConnectionEstablished
  }

  override def onError(errorMessage: String): Unit = {
    dispatcher ! ErrorMessage(errorMessage)
  }

  override def onMessage(formicMessage: FormicMessage): Unit = {
    dispatcher ! formicMessage
  }

  override def send(formicMessage: FormicMessage): Unit = {
    webSocketConnection.send(write(formicMessage))
  }
}