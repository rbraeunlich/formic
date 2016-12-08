package de.tu_berlin.formic.client

import akka.actor.{ActorSystem, Props}
import akka.testkit.{EventFilter, TestActorRef, TestKit, TestProbe}
import com.typesafe.config.ConfigFactory
import de.tu_berlin.formic.client.Dispatcher.WrappedUpdateResponse
import de.tu_berlin.formic.client.WebSocketConnection.{OnConnect, OnError, OnMessage}
import de.tu_berlin.formic.common.datatype.OperationContext
import de.tu_berlin.formic.common.datatype.client.AbstractClientDataTypeFactory.NewDataTypeCreated
import de.tu_berlin.formic.common.json.FormicJsonProtocol
import de.tu_berlin.formic.common.json.FormicJsonProtocol._
import de.tu_berlin.formic.common.message._
import de.tu_berlin.formic.common.{ClientId, DataTypeInstanceId, OperationId}
import org.scalajs.dom.raw.ErrorEvent
import org.scalajs.dom.{CloseEvent, Event, MessageEvent, WebSocket}
import org.scalatest._
import upickle.default._

import scala.concurrent.Future
import scala.concurrent.duration._
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
  with OneInstancePerTest
  with BeforeAndAfterAll
  with Matchers {

  import system.dispatcher

  override def newInstance: Suite with OneInstancePerTest = new WebSocketConnectionSpec

  override def beforeAll(): Unit = {
    FormicJsonProtocol.registerProtocol(new TestFormicJsonDataTypeProtocol)
  }

  override def afterAll(): Unit = {
    FormicJsonProtocol.remove(TestClasses.dataTypeName)
    system.terminate()
  }

  val timeout: FiniteDuration = 10.seconds

  "WebSocketConnection" must {

    "register for onopen events" in {
      val factory = new TestWebSocketFactory
      val connection: TestActorRef[WebSocketConnection] = TestActorRef(Props(new WebSocketConnection(TestProbe().ref, TestProbe().ref, ClientId(), factory , "")))

      awaitAssert(factory.mock.asInstanceOf[WebSocket].onopen should not be null, timeout)
    }

    "create a dispatcher after connecting" ignore {
      val connection: TestActorRef[WebSocketConnection] = TestActorRef(Props(new WebSocketConnection(TestProbe().ref, TestProbe().ref, ClientId(), new TestWebSocketFactory, "")))

      connection ! OnConnect

      connection.underlyingActor.dispatcher should not equal null
    }

    "register for onerror and onmessage events" in {
      val factory = new TestWebSocketFactory
      val connection: TestActorRef[WebSocketConnection] = TestActorRef(Props(new WebSocketConnection(TestProbe().ref, TestProbe().ref, ClientId(), factory, "")))

      connection ! OnConnect

      awaitAssert(factory.mock.asInstanceOf[WebSocket].onmessage should not be null, timeout)
    }

    "forward error messages to the dispatcher" ignore {
      //EventFilter does not work yet in AkkaJS
      val connection = system.actorOf(Props(new WebSocketConnection(TestProbe().ref, TestProbe().ref, ClientId(), new TestWebSocketFactory, "")))
      system.scheduler.scheduleOnce(0.millis) {
        connection ! OnConnect

        EventFilter.debug(message = "Received OnError message", occurrences = 1) intercept {
          connection ! OnError("test")
        }
      }
    }

    "forward CreateResponses from the WebSocketConnection to the dispatcher" ignore {
      val createResponse = CreateResponse(DataTypeInstanceId())
      val connection: TestActorRef[WebSocketConnection] = TestActorRef(Props(new WebSocketConnection(TestProbe().ref, TestProbe().ref, ClientId(), new TestWebSocketFactory, "")))
      //TODO
      system.scheduler.scheduleOnce(0.millis) {
        connection ! OnConnect

        connection ! OnMessage(write(createResponse))
      }
    }

    "forward UpdateResponses from the WebSocketConnection to the dispatcher" in {
      val updateResponse = UpdateResponse(DataTypeInstanceId(), TestClasses.dataTypeName, "", Option.empty)
      val instantiator = TestProbe()
      val connection: TestActorRef[WebSocketConnection] = TestActorRef(Props(new WebSocketConnection(TestProbe().ref, instantiator.ref, ClientId(), new TestWebSocketFactory, "")))
      connection ! OnConnect

      connection ! OnMessage(write(updateResponse))

      instantiator.expectMsg(timeout, WrappedUpdateResponse(connection, updateResponse))
    }

    "forward OperationMessages from the WebSocketConnection to the dispatcher" in {
      val dataTypeInstance = TestProbe()
      val dataTypeInstanceId = DataTypeInstanceId()
      val updateResponse = UpdateResponse(dataTypeInstanceId, TestClasses.dataTypeName, "", Option.empty)
      val instantiator = new TestProbe(system) {
        def answerUpdateResponse() = {
          expectMsgPF(timeout) {
            case rep: WrappedUpdateResponse => rep.updateResponse should equal(updateResponse)
          }
          sender ! NewDataTypeCreated(dataTypeInstanceId, dataTypeInstance.ref, null)
        }
      }
      val operationMessage = OperationMessage(ClientId(), dataTypeInstanceId, TestClasses.dataTypeName, List.empty)
      val connection: TestActorRef[WebSocketConnection] = TestActorRef(Props(new WebSocketConnection(TestProbe().ref, instantiator.ref, ClientId(), new TestWebSocketFactory, "")))
      connection ! OnConnect
      connection ! OnMessage(write(updateResponse))
      instantiator.answerUpdateResponse()


      connection ! OnMessage(write(operationMessage))
      dataTypeInstance.expectMsg(operationMessage)
    }

    "add the ClientId to CreateRequests and send them over the WebSocket" in {
      val request = CreateRequest(null, DataTypeInstanceId(), TestClasses.dataTypeName)
      val probe = TestProbe()
      val factory = new TestWebSocketFactory
      val clientId = ClientId()
      val connection: TestActorRef[WebSocketConnection] = TestActorRef(Props(new WebSocketConnection(TestProbe().ref, TestProbe().ref, clientId, factory, "")))
      connection ! OnConnect

      connection ! (probe.ref, request)

      awaitCond(factory.mock.sent.nonEmpty, timeout)
      val sentMessages = factory.mock.sent
      sentMessages.headOption match {
        case Some(msg) => read[FormicMessage](msg.asInstanceOf[String]) should equal(CreateRequest(clientId, request.dataTypeInstanceId, request.dataType))
        case None => fail("No message sent via WebSocket")
      }
    }
    "add the ClientId to HistoricOperationRequests and send them over the WebSocket" in {
      val request = HistoricOperationRequest(null, DataTypeInstanceId(), OperationId())
      val factory = new TestWebSocketFactory
      val clientId = ClientId()
      val connection: TestActorRef[WebSocketConnection] = TestActorRef(Props(new WebSocketConnection(TestProbe().ref, TestProbe().ref, clientId, factory, "")))
      connection ! OnConnect

      connection ! request

      awaitCond(factory.mock.sent.nonEmpty, timeout)
      val sentMessages = factory.mock.sent
      sentMessages.headOption match {
        case Some(msg) => read[FormicMessage](msg.asInstanceOf[String]) should equal(HistoricOperationRequest(clientId, request.dataTypeInstanceId, request.sinceId))
        case None => fail("No message sent via WebSocket")
      }
    }

    "add the ClientId to UpdateRequests and send them over the WebSocket" in {
      val request = UpdateRequest(null, DataTypeInstanceId())
      val factory = new TestWebSocketFactory
      val clientId = ClientId()
      val connection: TestActorRef[WebSocketConnection] = TestActorRef(Props(new WebSocketConnection(TestProbe().ref, TestProbe().ref, clientId, factory, "")))
      connection ! OnConnect

      connection ! request

      awaitCond(factory.mock.sent.nonEmpty, timeout)
      val sentMessages = factory.mock.sent
      sentMessages.headOption match {
        case Some(msg) => read[FormicMessage](msg.asInstanceOf[String]) should equal(UpdateRequest(clientId, request.dataTypeInstanceId))
        case None => fail("No message sent via WebSocket")
      }
    }

    "add the ClientId to OperationMessages and send them over the WebSocket" in {
      val message = OperationMessage(null, DataTypeInstanceId(), TestClasses.dataTypeName, List(TestOperation(OperationId(), OperationContext(List.empty), null)))
      val factory = new TestWebSocketFactory
      val clientId = ClientId()
      val connection: TestActorRef[WebSocketConnection] = TestActorRef(Props(new WebSocketConnection(TestProbe().ref, TestProbe().ref, clientId, factory, "")))
      connection ! OnConnect

      connection ! message

      awaitCond(factory.mock.sent.nonEmpty, timeout)
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

  override def createConnection(url: String): WebSocket = {
    mock.asInstanceOf[WebSocket]
  }
}

@JSExportAll
class WebSocketMock {
  var sent: List[String] = List.empty
  val isInitialized = true

  def send(data: String) = {
    sent = sent :+ data
  }

  var onopen: js.Function1[Event, _] = _
  var onmessage: js.Function1[MessageEvent, _] = _
  var onclose: js.Function1[CloseEvent, _] = _
  var onerror: js.Function1[ErrorEvent, _] = _
}