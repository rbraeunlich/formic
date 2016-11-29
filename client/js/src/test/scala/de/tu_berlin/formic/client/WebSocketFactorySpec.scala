package de.tu_berlin.formic.client

import org.scalajs.dom.raw.WebSocket
import org.scalatest.{FlatSpec, Matchers}

/**
  * @author Ronny Bräunlich
  */
class WebSocketFactorySpec extends FlatSpec with Matchers{

  "WebSocketFactory" should "open a WebSocket connection" in {
    val connection = WebSocketFactory.createConnection("ws://echo.websocket.org")

    connection.readyState should equal(WebSocket.OPEN)
  }

}
