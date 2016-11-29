package de.tu_berlin.formic.client

import org.scalajs.dom.raw.WebSocket
import org.scalatest.{BeforeAndAfterAll, FlatSpec, Matchers}

/**
  * @author Ronny Bräunlich
  */
class WebSocketFactorySpec extends FlatSpec with Matchers with BeforeAndAfterAll {

  var connection: WebSocket = _

  override def afterAll(): Unit = {
   if(connection != null) connection.close()
  }

  "WebSocketFactory" should "open a WebSocket connection" in {
    connection = WebSocketFactory.createConnection("ws://test:1234@localhost:8080/formic")
    connection.send("data")
  }
}
