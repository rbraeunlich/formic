package de.tu_berlin.formic.client

import de.tu_berlin.formic.client.WebSocketFactorySpec.{WebSocketClosedState, WebSocketClosingState, WebSocketConnectingState, WebSocketOpenState}

import scala.scalajs.js.timers._
import org.scalajs.dom._
import org.scalajs.dom.raw.WebSocket
import org.scalatest.{BeforeAndAfterAll, FlatSpec, Matchers}

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

    setTimeout(500) {
      getConnectionObject(connection.readyState) should equal(WebSocketFactorySpec.WebSocketOpenState)
    }
    setTimeout(2000) {
      connection.close()
    }
  }

  /**
    * This method and the states only serve the readability of the TestFailedException
    */
  def getConnectionObject(readyState: Int) = {
    readyState match {
      case 0 => WebSocketConnectingState
      case 1 => WebSocketOpenState
      case 2 => WebSocketClosingState
      case 3 => WebSocketClosedState
    }
  }
}

object WebSocketFactorySpec {
  sealed trait WebSocketState {
    val stateNr: Int
  }
  case object WebSocketConnectingState extends WebSocketState {
    override val stateNr: Int = 0
  }
  case object WebSocketOpenState extends WebSocketState {
    override val stateNr: Int = 1
  }
  case object WebSocketClosingState extends WebSocketState {
    override val stateNr: Int = 2
  }
  case object WebSocketClosedState extends WebSocketState {
    override val stateNr: Int = 3
  }
}
