package de.tu_berlin.formic.client

import org.scalajs.dom.WebSocket

/**
  * @author Ronny Bräunlich
  */
class WrappedJSWebSocket(val webSocket: WebSocket) extends WebSocketWrapper {

  override def send(message: String): Unit = webSocket.send(message)

}
