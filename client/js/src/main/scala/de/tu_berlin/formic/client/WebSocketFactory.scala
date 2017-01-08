package de.tu_berlin.formic.client

import akka.actor.ActorRef
import de.tu_berlin.formic.client.WebSocketConnection.{OnClose, OnConnect, OnError, OnMessage}
import org.scalajs.dom
import org.scalajs.dom._

/**
  * @author Ronny Bräunlich
  */
trait WebSocketFactory {

  def createConnection(url: String, connection: ActorRef): WebSocketWrapper

}

object WebSocketFactory extends WebSocketFactory {

  override def createConnection(url: String, connection: ActorRef): WebSocketWrapper = {
    val ws = new WebSocket(url)
    val wrapper = new WrappedJSWebSocket(ws)
    ws.onopen = { event: Event => connection ! OnConnect(wrapper) }
    ws.onerror = { event: ErrorEvent => connection ! OnError(event.message) }
    ws.onmessage = { event: MessageEvent => connection ! OnMessage(event.data.toString) }
    ws.onclose = { event: CloseEvent => connection ! OnClose(event.code) }
    wrapper
  }

}