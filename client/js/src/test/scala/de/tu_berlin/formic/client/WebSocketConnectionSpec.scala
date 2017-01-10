package de.tu_berlin.formic.client

import akka.actor.{ActorRef, ActorSystem, PoisonPill, Props, Terminated}
import akka.testkit.{EventFilter, TestActorRef, TestKit, TestProbe}
import com.typesafe.config.ConfigFactory
import de.tu_berlin.formic.client.Dispatcher.WrappedUpdateResponse
import de.tu_berlin.formic.client.WebSocketConnection._
import de.tu_berlin.formic.common.datatype.OperationContext
import de.tu_berlin.formic.common.datatype.client.AbstractClientDataTypeFactory.NewDataTypeCreated
import de.tu_berlin.formic.common.json.FormicJsonProtocol
import de.tu_berlin.formic.common.json.FormicJsonProtocol._
import de.tu_berlin.formic.common.message._
import de.tu_berlin.formic.common.{ClientId, DataTypeInstanceId, OperationId}
import org.scalatest._
import upickle.default._

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.{Failure, Success}

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

    "create a dispatcher" in {
      val connection: TestActorRef[WebSocketConnection] = TestActorRef(Props(new WebSocketConnection(TestProbe().ref, TestProbe().ref, ClientId(), new TestWebSocketFactory, "1")))
      val f = Future {
        val result = connection.underlyingActor.dispatcher should not equal null
        //explicitely kill the actor or else the running job won't stop
        connection ! PoisonPill
        result
      }
      system.scheduler.scheduleOnce(0.millis)(f)
      awaitCond(f.isCompleted, timeout)
      f.value.get match {
        case Success(result) => result
        case Failure(ex) => fail(ex)
      }
    }

    "forward error messages to the dispatcher when being offline" in {
      val factory = new TestWebSocketFactory
      val newInstanceCallback = TestProbe()
      val instantiator = TestProbe()
      val connection: TestActorRef[WebSocketConnection] = TestActorRef(Props(new WebSocketConnection(newInstanceCallback.ref, instantiator.ref, ClientId(), factory, "2")))
      //this serves as synchronization point
      awaitAssert(connection.underlyingActor.dispatcher should not equal null, timeout)
      EventFilter.error(message = "Error from WebSocket connection: test", occurrences = 1) intercept {
        connection ! OnError("test")
      }
      //explicitely kill the actor or else the running job won't stop
      connection ! PoisonPill
    }

    "forward error messages to the dispatcher when being online" in {
      val factory = new TestWebSocketFactory
      val newInstanceCallback = TestProbe()
      val instantiator = TestProbe()
      val connection: TestActorRef[WebSocketConnection] = TestActorRef(Props(new WebSocketConnection(newInstanceCallback.ref, instantiator.ref, ClientId(), factory, "3")))
      system.scheduler.scheduleOnce(0.millis) {
        connection ! OnConnect(factory.mock)
      }
      //this serves as synchronization point
      awaitAssert(connection.underlyingActor.dispatcher should not equal null, timeout)
      EventFilter.error(message = "Error from WebSocket connection: test", occurrences = 1) intercept {
        connection ! OnError("test")
      }
      //explicitely kill the actor or else the running job won't stop
      connection ! PoisonPill
    }

    "forward CreateResponses from the WebSocketConnection to the dispatcher" in {
      val dataTypeInstanceId = DataTypeInstanceId()
      val factory = new TestWebSocketFactory
      val newInstanceCallback = TestProbe()
      val instantiator = TestProbe()
      val createResponse = CreateResponse(dataTypeInstanceId)
      val connection: TestActorRef[WebSocketConnection] = TestActorRef(Props(new WebSocketConnection(newInstanceCallback.ref, instantiator.ref, ClientId(), factory, "5")))
      val dataType = TestProbe()
      system.scheduler.scheduleOnce(0.millis) {
        connection ! OnConnect(factory.mock)
        val dispatcher = connection.getSingleChild("dispatcher")
        dispatcher ! NewDataTypeCreated(dataTypeInstanceId, dataType.ref, new TestFormicDataType())

        connection ! OnMessage(write(createResponse))
      }
      dataType.expectMsg(timeout, createResponse)
    }

    "forward UpdateResponses from the WebSocketConnection to the dispatcher" in {
      val updateResponse = UpdateResponse(DataTypeInstanceId(), TestClasses.dataTypeName, "", Option.empty)
      val newInstanceCallback = TestProbe()
      val instantiator = TestProbe()
      val factory = new TestWebSocketFactory
      val connection: TestActorRef[WebSocketConnection] = TestActorRef(Props(new WebSocketConnection(newInstanceCallback.ref, instantiator.ref, ClientId(), factory, "6")))
      system.scheduler.scheduleOnce(0.millis) {
        connection ! OnConnect(factory.mock)

        connection ! OnMessage(write(updateResponse))
      }
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
      val factory = new TestWebSocketFactory
      val newInstanceCallback = TestProbe()
      val connection: TestActorRef[WebSocketConnection] = TestActorRef(Props(new WebSocketConnection(newInstanceCallback.ref, instantiator.ref, ClientId(), factory, "7")))
      system.scheduler.scheduleOnce(0.millis) {
        connection ! OnConnect(factory.mock)
        connection ! OnMessage(write(updateResponse))
        instantiator.answerUpdateResponse()

        connection ! OnMessage(write(operationMessage))
      }
      dataTypeInstance.expectMsg(operationMessage)
    }

    "add the ClientId to CreateRequests and send them over the WebSocket" in {
      val request = CreateRequest(null, DataTypeInstanceId(), TestClasses.dataTypeName)
      val probe = TestProbe()
      val factory = new TestWebSocketFactory
      val clientId = ClientId()
      val newInstanceCallback = TestProbe()
      val instantiator = TestProbe()
      val connection: TestActorRef[WebSocketConnection] = TestActorRef(Props(new WebSocketConnection(newInstanceCallback.ref, instantiator.ref, clientId, factory, "8")))
      system.scheduler.scheduleOnce(0.millis) {
        connection ! OnConnect(factory.mock)

        connection ! (probe.ref, request)
      }
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
      val newInstanceCallback = TestProbe()
      val instantiator = TestProbe()
      val connection: TestActorRef[WebSocketConnection] = TestActorRef(Props(new WebSocketConnection(newInstanceCallback.ref, instantiator.ref, clientId, factory, "9")))
      system.scheduler.scheduleOnce(0.millis) {
        connection ! OnConnect(factory.mock)

        connection ! request
      }
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
      val newInstanceCallback = TestProbe()
      val instantiator = TestProbe()
      val connection: TestActorRef[WebSocketConnection] = TestActorRef(Props(new WebSocketConnection(newInstanceCallback.ref, instantiator.ref, clientId, factory, "10")))
      system.scheduler.scheduleOnce(0.millis) {
        connection ! OnConnect(factory.mock)

        connection ! request
      }
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
      val newInstanceCallback = TestProbe()
      val instantiator = TestProbe()
      val connection: TestActorRef[WebSocketConnection] = TestActorRef(Props(new WebSocketConnection(newInstanceCallback.ref, instantiator.ref, clientId, factory, "11")))
      system.scheduler.scheduleOnce(0.millis) {
        connection ! OnConnect(factory.mock)

        connection ! message
      }
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

    "become offline after receiving an OnClose message" in {
      val watcher = TestProbe()
      val factory = new TestWebSocketFactory
      val newInstanceCallback = TestProbe()
      val instantiator = TestProbe()
      val connection: TestActorRef[WebSocketConnection] = TestActorRef(Props(new WebSocketConnection(newInstanceCallback.ref, instantiator.ref, ClientId(), factory, "13")))
      system.scheduler.scheduleOnce(0.millis) {
        watcher watch connection
        connection ! OnConnect(factory.mock)

        connection ! OnClose(1)
        connection ! UpdateRequest(null, DataTypeInstanceId())
        //explicitely kill the actor or else the running job won't stop
        connection ! PoisonPill
      }
      watcher.expectMsgClass(classOf[Terminated])
      awaitAssert(factory.mock.sent shouldBe empty, timeout)
    }

    "buffer outgoing CreateRequests after becoming offline and send them after becoming connected again" in {
      val clientId = ClientId()
      val factory = new TestWebSocketFactory
      val newInstanceCallback = TestProbe()
      val instantiator = TestProbe()
      val connection: TestActorRef[WebSocketConnection] = TestActorRef(Props(new WebSocketConnection(newInstanceCallback.ref, instantiator.ref, clientId, factory, "14")))
      val request = CreateRequest(null, DataTypeInstanceId(), TestClasses.dataTypeName)
      val probe = TestProbe()
      system.scheduler.scheduleOnce(0.millis) {
        connection ! OnConnect(factory.mock)
        connection ! OnClose(1)
        connection ! (probe.ref, request)
      }
      awaitAssert(factory.mock.sent shouldBe empty, timeout)

      connection ! OnConnect(factory.mock)

      awaitCond(factory.mock.sent.nonEmpty, timeout)
      //explicitely kill the actor or else the running job won't stop
      connection ! PoisonPill
      val sentMessages = factory.mock.sent
      sentMessages.headOption match {
        case Some(msg) => read[FormicMessage](msg.asInstanceOf[String]) should equal(CreateRequest(clientId, request.dataTypeInstanceId, TestClasses.dataTypeName))
        case None => fail("No message sent via WebSocket")
      }
    }

    "buffer outgoing HistoricOperationRequests after becoming offline and send them after becoming connected again" in {
      val clientId = ClientId()
      val factory = new TestWebSocketFactory
      val newInstanceCallback = TestProbe()
      val instantiator = TestProbe()
      val connection: TestActorRef[WebSocketConnection] = TestActorRef(Props(new WebSocketConnection(newInstanceCallback.ref, instantiator.ref, clientId, factory, "15")))
      val request = HistoricOperationRequest(null, DataTypeInstanceId(), OperationId())
      system.scheduler.scheduleOnce(0.millis) {
        connection ! OnConnect(factory.mock)
        connection ! OnClose(1)

        connection ! request
      }
      awaitAssert(factory.mock.sent shouldBe empty, timeout)

      connection ! OnConnect(factory.mock)

      awaitCond(factory.mock.sent.nonEmpty, timeout)
      //explicitely kill the actor or else the running job won't stop
      connection ! PoisonPill
      val sentMessages = factory.mock.sent
      sentMessages.headOption match {
        case Some(msg) => read[FormicMessage](msg.asInstanceOf[String]) should equal(HistoricOperationRequest(clientId, request.dataTypeInstanceId, request.sinceId))
        case None => fail("No message sent via WebSocket")
      }
    }

    "buffer outgoing UpdateRequests after becoming offline and send them after becoming connected again" in {
      val clientId = ClientId()
      val factory = new TestWebSocketFactory
      val newInstanceCallback = TestProbe()
      val instantiator = TestProbe()
      val connection: TestActorRef[WebSocketConnection] = TestActorRef(Props(new WebSocketConnection(newInstanceCallback.ref, instantiator.ref, clientId, factory, "16")))
      val request = UpdateRequest(null, DataTypeInstanceId())
      system.scheduler.scheduleOnce(0.millis) {
        connection ! OnConnect(factory.mock)
        connection ! OnClose(1)
        connection ! request
      }
      awaitAssert(factory.mock.sent shouldBe empty, timeout)

      connection ! OnConnect(factory.mock)

      awaitCond(factory.mock.sent.nonEmpty, timeout)
      //explicitely kill the actor or else the running job won't stop
      connection ! PoisonPill
      val sentMessages = factory.mock.sent
      sentMessages.headOption match {
        case Some(msg) => read[FormicMessage](msg.asInstanceOf[String]) should equal(UpdateRequest(clientId, request.dataTypeInstanceId))
        case None => fail("No message sent via WebSocket")
      }
    }

    "buffer outgoing OperationMessages after becoming offline and send them after becoming connected again" in {
      val clientId = ClientId()
      val factory = new TestWebSocketFactory
      val newInstanceCallback = TestProbe()
      val instantiator = TestProbe()
      val connection: TestActorRef[WebSocketConnection] = TestActorRef(Props(new WebSocketConnection(newInstanceCallback.ref, instantiator.ref, clientId, factory, "17")))
      val message = OperationMessage(null, DataTypeInstanceId(), TestClasses.dataTypeName, List(TestOperation(OperationId(), OperationContext(List.empty), null)))
      system.scheduler.scheduleOnce(0.millis) {
        connection ! OnConnect(factory.mock)
        connection ! OnClose(1)
        connection ! message
      }
      awaitAssert(factory.mock.sent shouldBe empty, timeout)

      connection ! OnConnect(factory.mock)

      awaitCond(factory.mock.sent.nonEmpty, timeout)
      //explicitely kill the actor or else the running job won't stop
      connection ! PoisonPill
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

  val mock = new WebSocketWrapper {

    var sent: List[String] = List.empty

    override def send(message: String): Unit = {
      sent = sent :+ message
    }
  }

  override def createConnection(url: String, connection: ActorRef): WebSocketWrapper = {
    mock
  }
}