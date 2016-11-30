package de.tu_berlin.formic.client

import akka.actor.{ActorSystem, Props}
import akka.testkit.{TestActorRef, TestKit, TestProbe}
import com.typesafe.config.ConfigFactory
import de.tu_berlin.formic.client.WebSocketConnection.OnConnect
import de.tu_berlin.formic.common.ClientId
import de.tu_berlin.formic.common.json.FormicJsonProtocol
import org.scalajs.dom.{CloseEvent, Event, MessageEvent, WebSocket}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.scalajs.js
import scala.scalajs.js.annotation.JSExportAll

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
      val connection: TestActorRef[WebSocketConnection] = TestActorRef(Props(new WebSocketConnection(TestProbe().ref, TestProbe().ref, ClientId(), new TestWebSocketFactory)))

      connection ! OnConnect

      connection.underlyingActor.dispatcher should not be null
    }
  }
}

class TestWebSocketFactory extends WebSocketFactory {
  /*
  This needs a little bit more explanation. Everything is related to the dynamic typing in JavaScript.
  Although the WebSocketMock does not extend WebSocket it behaves like one, therefore we can cast it to WebSocket.
  Unfortunately, we cannot directly mock the constructor. Because of that, we mock the constructor with a separate
  JS function, that returns the mock. Because the function mocks the WebSocket constructor we cast it to WebSocket, too.
   */
  val mock = new WebSocketMock

  override def createConnection(url: String): WebSocket = {
    mock.asInstanceOf[WebSocket]
  }
}

@JSExportAll
class WebSocketMock {
  var sent: List[js.Any] = List.empty
  val isInitialized = true

  def send[T](data: js.Any) = {
    sent = sent :+ data
  }

  def onmessage(callback: js.Function1[MessageEvent, Unit]): Unit = {}

  def onclose(callback: js.Function1[CloseEvent, Unit]): Unit = {}

  def onopen(callback: js.Function1[Event, Unit]): Unit = {}
}