package de.tu_berlin.formic.client

import akka.actor.{ActorSystem, Props}
import akka.testkit.{EventFilter, TestActorRef, TestKit, TestProbe}
import com.typesafe.config.ConfigFactory
import de.tu_berlin.formic.client.Dispatcher.ErrorMessage
import de.tu_berlin.formic.client.WebSocketConnection.{OnConnect, OnError, OnMessage}
import de.tu_berlin.formic.common.datatype.{DataTypeName, OperationContext}
import de.tu_berlin.formic.common.json.FormicJsonProtocol
import de.tu_berlin.formic.common.{ClientId, DataTypeInstanceId, OperationId}
import de.tu_berlin.formic.common.message._
import org.scalajs.dom.{CloseEvent, MessageEvent, WebSocket}
import org.scalatest.{BeforeAndAfterAll, Ignore, Matchers, WordSpecLike}
import upickle.default._
import de.tu_berlin.formic.common.json.FormicJsonProtocol._

import scala.scalajs.js
import scala.scalajs.js.UndefOr
import scala.scalajs.js.annotation.JSExportAll
import scala.concurrent.duration._

/**
  * @author Ronny Bräunlich
  */
@Ignore
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
    "create a dispatcher after connecting" ignore {
      val connection: TestActorRef[WebSocketConnection] = TestActorRef(Props(new WebSocketConnection(TestProbe().ref, TestProbe().ref, ClientId(),new TestWebSocketFactory)))

      connection ! OnConnect

      connection.underlyingActor.dispatcher should not be null
    }

    "forward error messages to the dispatcher" ignore {
      val connection = system.actorOf(Props(new WebSocketConnection(TestProbe().ref, TestProbe().ref, ClientId(),new TestWebSocketFactory)))
      connection ! OnConnect

      EventFilter.debug(message = "Received OnError message", occurrences = 1) intercept {
        connection ! OnError("test")
      }
    }

    "forward FormicMessages from the WebSocketConnection to the dispatcher" ignore {
      val createResponse = CreateResponse(DataTypeInstanceId())
      val updateResponse = UpdateResponse(DataTypeInstanceId(), TestClasses.dataTypeName, "")
      val operationMessage = OperationMessage(ClientId(), DataTypeInstanceId(), TestClasses.dataTypeName, List.empty)
      val dispatcher = TestProbe()
      val connection: TestActorRef[WebSocketConnection] = TestActorRef(Props(new WebSocketConnection(TestProbe().ref, dispatcher.ref, ClientId(),new TestWebSocketFactory)))
      connection ! OnConnect

      connection ! OnMessage(write(createResponse))
      dispatcher.expectMsg(createResponse)

      connection ! OnMessage(write(updateResponse))
      dispatcher.expectMsg(updateResponse)

      connection ! OnMessage(write(operationMessage))
      dispatcher.expectMsg(operationMessage)
    }

    "add the ClientId to CreateRequests and send them over the WebSocket" ignore {
      val request = CreateRequest(null, DataTypeInstanceId(), TestClasses.dataTypeName)
      val factory = new TestWebSocketFactory
      val clientId = ClientId()
      val connection: TestActorRef[WebSocketConnection] = TestActorRef(Props(new WebSocketConnection(TestProbe().ref, TestProbe().ref, clientId, factory)))
      connection ! OnConnect

      connection ! request

      val sentMessages = factory.mock.sent
      sentMessages.headOption match {
        case Some(msg) => read[FormicMessage](msg.asInstanceOf[String]) should equal(CreateRequest(clientId, request.dataTypeInstanceId, request.dataType))
        case None => fail("No message sent via WebSocket")
      }
    }

    "add the ClientId to HistoricOperationRequests and send them over the WebSocket" ignore {
      val request = HistoricOperationRequest(null, DataTypeInstanceId(), OperationId())
      val factory = new TestWebSocketFactory
      val clientId = ClientId()
      val connection: TestActorRef[WebSocketConnection] = TestActorRef(Props(new WebSocketConnection(TestProbe().ref, TestProbe().ref, clientId, factory)))
      connection ! OnConnect

      connection ! request

      val sentMessages = factory.mock.sent
      sentMessages.headOption match {
        case Some(msg) => read[FormicMessage](msg.asInstanceOf[String]) should equal(HistoricOperationRequest(clientId, request.dataTypeInstanceId, request.sinceId))
        case None => fail("No message sent via WebSocket")
      }
    }

    "add the ClientId to UpdateRequests and send them over the WebSocket" ignore {
      val request = UpdateRequest(null, DataTypeInstanceId())
      val factory = new TestWebSocketFactory
      val clientId = ClientId()
      val connection: TestActorRef[WebSocketConnection] = TestActorRef(Props(new WebSocketConnection(TestProbe().ref, TestProbe().ref, clientId, factory)))
      connection ! OnConnect

      connection ! request

      val sentMessages = factory.mock.sent
      sentMessages.headOption match {
        case Some(msg) => read[FormicMessage](msg.asInstanceOf[String]) should equal(UpdateRequest(clientId, request.dataTypeInstanceId))
        case None => fail("No message sent via WebSocket")
      }
    }

    "add the ClientId to OperationMessages and send them over the WebSocket" ignore {
      val message = OperationMessage(null, DataTypeInstanceId(), TestClasses.dataTypeName, List(TestOperation(OperationId(), OperationContext(List.empty), null)))
      val factory = new TestWebSocketFactory
      val clientId = ClientId()
      val connection: TestActorRef[WebSocketConnection] = TestActorRef(Props(new WebSocketConnection(TestProbe().ref, TestProbe().ref, clientId, factory)))
      connection ! OnConnect

      connection ! message

      val sentMessages = factory.mock.sent
      sentMessages.headOption match {
        case Some(msg) =>
          val sentOperation = message.operations.head
          read[FormicMessage](msg.asInstanceOf[String]) should equal(
            OperationMessage(clientId, message.dataTypeInstanceId, message.dataType, List(TestOperation(sentOperation.id, sentOperation.operationContext, clientId)))
          )
        case None => fail("No message sent via WebSocket")
      }
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
  val mockedConstructor: js.Function = { (url: String, options: js.UndefOr[js.Dynamic]) =>
    mock.asInstanceOf[WebSocket]
  }
  override def createConnection(url: String): WebSocket = {
    mockedConstructor.asInstanceOf[WebSocket]
  }
}

@JSExportAll
class WebSocketMock {
  var sent:List[js.Any] = List.empty
  val isInitialized = true
  def send[T](data: js.Any) ={
    sent = sent :+ data}
  def onMessage(callback: js.Function1[MessageEvent, Unit]): Unit = {}
  def onClose(callback: js.Function1[CloseEvent, Unit]): Unit = {}
  def onOpen(callback: js.Function1[js.Dynamic, Unit]): Unit = {}
}