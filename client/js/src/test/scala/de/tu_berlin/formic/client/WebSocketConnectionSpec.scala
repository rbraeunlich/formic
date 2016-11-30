package de.tu_berlin.formic.client

import akka.actor.{ActorSystem, Props}
import akka.testkit.{TestActorRef, TestKit, TestProbe}
import com.typesafe.config.ConfigFactory
import de.tu_berlin.formic.client.WebSocketConnection.OnConnect
import de.tu_berlin.formic.common.ClientId
import de.tu_berlin.formic.common.json.FormicJsonProtocol
import org.scalajs.dom.WebSocket
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.scalajs.js

/**
  * @author Ronny Bräunlich
  */
class WebSocketConnectionSpec extends TestKit(ActorSystem("WebSocketConnectionSpec", ConfigFactory.parseString(
  """
  akka.loggers = ["akka.testkit.TestEventListener"]
  """)))
  with WordSpecLike
  with BeforeAndAfterAll
  with Matchers {

  override def beforeAll(): Unit = {
    FormicJsonProtocol.registerProtocol(new TestFormicJsonDataTypeProtocol)
  }

  override def afterAll(): Unit = {
    FormicJsonProtocol.remove(TestClasses.dataTypeName)
    system.terminate()
  }

  "WebSocketConnection" must {
    "create a dispatcher after connecting" in {
      val connection: TestActorRef[WebSocketConnection] = TestActorRef(Props(new WebSocketConnection(TestProbe().ref, TestProbe().ref, ClientId(), TestWebSocketFactory)))

      connection ! OnConnect

      connection.underlyingActor.dispatcher should not be null
    }
  }
}

object TestWebSocketFactory extends WebSocketFactory {

  override def createConnection(url: String): WebSocket = new js.Object().asInstanceOf[WebSocket]

}