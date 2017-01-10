package de.tu_berlin.formic.client

import akka.actor.{Actor, ActorRef}
import akka.event.Logging.StandardOutLogger
import de.tu_berlin.formic.client.WebSocketFactoryJSSpec._
import org.scalatest.{FlatSpec, Matchers}

import scala.scalajs.js.timers._

/**
  * @author Ronny Bräunlich
  */
class WebSocketFactoryJSSpec extends FlatSpec with Matchers {

  "WebSocketFactoryJS" should "open a WebSocket connection" in {

    val connection = WebSocketFactoryJS.createConnection("ws://echo.websocket.org", new StandardOutLogger)

    setTimeout(500) {
      getConnectionObject(connection.asInstanceOf[WrappedJSWebSocket].webSocket.readyState) should equal(WebSocketFactoryJSSpec.WebSocketOpenState)
    }
    setTimeout(2000) {
      connection.asInstanceOf[WrappedJSWebSocket].webSocket.close()
    }
  }

  it should "register for onopen events" in {
    val connection = WebSocketFactoryJS.createConnection("ws://echo.websocket.org", new StandardOutLogger)
    connection.asInstanceOf[WrappedJSWebSocket].webSocket.onopen should not be null
  }

  it should "register for onerror events" in {
    val connection = WebSocketFactoryJS.createConnection("ws://echo.websocket.org", new StandardOutLogger)
    connection.asInstanceOf[WrappedJSWebSocket].webSocket.onerror should not be null
  }

  it should "register for onmessage events" in {
    val connection = WebSocketFactoryJS.createConnection("ws://echo.websocket.org", new StandardOutLogger)
    connection.asInstanceOf[WrappedJSWebSocket].webSocket.onmessage should not be null
  }

  it should "register for onclose events" in {
    val connection = WebSocketFactoryJS.createConnection("ws://echo.websocket.org", new StandardOutLogger)
    connection.asInstanceOf[WrappedJSWebSocket].webSocket.onclose should not be null
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

object WebSocketFactoryJSSpec {
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
