package de.tu_berlin.formic.client

import akka.actor.{Actor, ActorSystem, Props}
import de.tu_berlin.formic.client.Dispatcher.ConnectionEstablished
import org.scalatest.{BeforeAndAfterAll, FlatSpec, Matchers}

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

/**
  * @author Ronny Bräunlich
  */
class WebSocketConnectionSpec extends FlatSpec with Matchers with BeforeAndAfterAll{

  var testServer: Server = null

  override def beforeAll(): Unit = {
    testServer = new Server("0.0.0.0:8080")
    testServer.on("connection", () => {testServer.send(js.Object(), js.Array())})
  }

  override def afterAll(): Unit = {
    testServer.stop({() => println("stopped")})
  }

  "WebSocketConnection" should "tell the dispatcher about established connections" in {
    val system = ActorSystem("WebSocketConnectionSpec")
    val dispatcher = system.actorOf(Props(TestDispatcher))
    val connection = new WebSocketConnection(dispatcher)
  }

}

object TestDispatcher extends Actor {

  def receive = {
    case ConnectionEstablished => println("works")
  }
}
