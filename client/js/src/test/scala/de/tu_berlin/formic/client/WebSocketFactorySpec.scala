package de.tu_berlin.formic.client

import de.tu_berlin.formic.client.WebSocketConnection.{OnError, OnMessage}
import org.scalajs.dom._
import org.scalajs.dom.raw.WebSocket
import org.scalatest.{BeforeAndAfterAll, FlatSpec, Matchers}

import scala.scalajs.js.timers._
/**
  * @author Ronny Bräunlich
  */
class WebSocketFactorySpec extends FlatSpec with Matchers {

  var connection: WebSocket = _

  "WebSocketFactory" should "open a WebSocket connection" in {
    connection = WebSocketFactory.createConnection("ws://echo.websocket.org")
    connection.onopen = { event: Event => println("WebSocketFactorySpec: connected")}
    connection.onerror = { event: ErrorEvent =>
      fail(event.message)
    }
    connection.onmessage = { event: MessageEvent => println(s"WebSocketFactorySpec: message received: $event") }

    setTimeout(1000) {
      connection.readyState should equal(WebSocket.OPEN)
    }
    setTimeout(2000) {
      connection.close()
    }
  }
}
