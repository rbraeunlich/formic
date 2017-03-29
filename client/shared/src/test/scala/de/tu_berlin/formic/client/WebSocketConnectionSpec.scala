package de.tu_berlin.formic.client

import akka.actor.{ActorRef, ActorSystem, PoisonPill, Props, Terminated}
import akka.testkit.{EventFilter, TestActorRef, TestKit, TestProbe}
import com.typesafe.config.ConfigFactory
import de.tu_berlin.formic.client.Dispatcher.WrappedUpdateResponse
import de.tu_berlin.formic.client.WebSocketConnection._
import de.tu_berlin.formic.common.datatype.OperationContext
import de.tu_berlin.formic.common.datatype.client.AbstractClientDataStructureFactory.NewDataTypeCreated
import de.tu_berlin.formic.common.json.FormicJsonProtocol
import de.tu_berlin.formic.common.message._
import de.tu_berlin.formic.common.{ClientId, DataStructureInstanceId, OperationId}
import org.scalatest._
import upickle.default._

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.{Failure, Success}

/**
  * @author Ronny Bräunlich
  */
class WebSocketConnectionSpec extends TestKit(ActorSystem("WebSocketConnectionSpec", ConfigFactory.parseString(
  """
  akka.loggers = ["akka.testkit.TestEventListener"]
  akka.loglevel = debug
  """)))
  with WordSpecLike
  with OneInstancePerTest
  with BeforeAndAfterAll
  with Matchers {

  import system.dispatcher

  override def newInstance: Suite with OneInstancePerTest = new WebSocketConnectionSpec

  override def afterAll(): Unit = {
    system.terminate()
  }

  val jsonProtocol = FormicJsonProtocol()
  jsonProtocol.registerProtocol(new TestFormicJsonDataTypeProtocol)

  implicit val writer = jsonProtocol.writer
  implicit val reader = jsonProtocol.reader


  val timeout: FiniteDuration = 10.seconds

  "WebSocketConnection" must {

    "create a dispatcher" in {
      val connection: TestActorRef[WebSocketConnection] = TestActorRef(Props(new WebSocketConnection(TestProbe().ref, TestProbe().ref, ClientId(), new TestWebSocketFactory, "1", 10, jsonProtocol)))
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
      val connection: TestActorRef[WebSocketConnection] = TestActorRef(Props(new WebSocketConnection(newInstanceCallback.ref, instantiator.ref, ClientId(), factory, "2", 10, jsonProtocol)))
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
      val connection: TestActorRef[WebSocketConnection] = TestActorRef(Props(new WebSocketConnection(newInstanceCallback.ref, instantiator.ref, ClientId(), factory, "3", 10, jsonProtocol)))
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
      val dataTypeInstanceId = DataStructureInstanceId()
      val factory = new TestWebSocketFactory
      val newInstanceCallback = TestProbe()
      val instantiator = TestProbe()
      val createResponse = CreateResponse(dataTypeInstanceId)
      val connection: TestActorRef[WebSocketConnection] = TestActorRef(Props(new WebSocketConnection(newInstanceCallback.ref, instantiator.ref, ClientId(), factory, "5", 10, jsonProtocol)))
      val dataType = TestProbe()
      system.scheduler.scheduleOnce(0.millis) {
        connection ! OnConnect(factory.mock)
        val dispatcher = connection.getSingleChild("dispatcher")
        dispatcher ! NewDataTypeCreated(dataTypeInstanceId, dataType.ref, new TestFormicDataStructure())

        connection ! OnMessage(write(createResponse))
      }
      dataType.expectMsg(timeout, createResponse)
    }

    "forward UpdateResponses from the WebSocketConnection to the dispatcher" in {
      val updateResponse = UpdateResponse(DataStructureInstanceId(), TestClasses.dataTypeName, "", Option.empty)
      val newInstanceCallback = TestProbe()
      val instantiator = TestProbe()
      val factory = new TestWebSocketFactory
      val connection: TestActorRef[WebSocketConnection] = TestActorRef(Props(new WebSocketConnection(newInstanceCallback.ref, instantiator.ref, ClientId(), factory, "6", 10, jsonProtocol)))
      system.scheduler.scheduleOnce(0.millis) {
        connection ! OnConnect(factory.mock)

        connection ! OnMessage(write(updateResponse))
      }
      instantiator.expectMsg(timeout, WrappedUpdateResponse(connection, updateResponse))
    }

    "forward OperationMessages from the WebSocketConnection to the dispatcher" in {
      val dataTypeInstance = TestProbe()
      val dataTypeInstanceId = DataStructureInstanceId()
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
      val connection: TestActorRef[WebSocketConnection] = TestActorRef(Props(new WebSocketConnection(newInstanceCallback.ref, instantiator.ref, ClientId(), factory, "7", 10, jsonProtocol)))
      system.scheduler.scheduleOnce(0.millis) {
        connection ! OnConnect(factory.mock)
        connection ! OnMessage(write(updateResponse))
        instantiator.answerUpdateResponse()

        connection ! OnMessage(write(operationMessage))
      }
      dataTypeInstance.expectMsg(operationMessage)
    }

    "become offline after receiving an OnClose message" in {
      val watcher = TestProbe()
      val factory = new TestWebSocketFactory
      val newInstanceCallback = TestProbe()
      val instantiator = TestProbe()
      val connection: TestActorRef[WebSocketConnection] = TestActorRef(Props(new WebSocketConnection(newInstanceCallback.ref, instantiator.ref, ClientId(), factory, "13", 10, jsonProtocol)))
      system.scheduler.scheduleOnce(0.millis) {
        watcher watch connection
        connection ! OnConnect(factory.mock)
        connection ! OnClose(1)
        connection ! UpdateRequest(null, DataStructureInstanceId())
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
      val connection: TestActorRef[WebSocketConnection] = TestActorRef(Props(new WebSocketConnection(newInstanceCallback.ref, instantiator.ref, clientId, factory, "14", 10, jsonProtocol)))
      val request = CreateRequest(clientId, DataStructureInstanceId(), TestClasses.dataTypeName)
      val probe = TestProbe()
      system.scheduler.scheduleOnce(0.millis) {
        connection ! OnConnect(factory.mock)
        connection ! OnClose(1)
        connection ! (probe.ref, request)
        awaitCond(factory.mock.sent.isEmpty, timeout)
        connection ! OnConnect(factory.mock)
      }
      awaitCond(factory.mock.sent.nonEmpty, timeout)
      //explicitely kill the actor or else the running job won't stop
      connection ! PoisonPill
      val sentMessages = factory.mock.sent
      sentMessages.lastOption match {
        case Some(msg) => read[FormicMessage](msg.asInstanceOf[String]) should equal(CreateRequest(clientId, request.dataStructureInstanceId, TestClasses.dataTypeName))
        case None => fail("No message sent via WebSocket")
      }
    }

    "buffer outgoing HistoricOperationRequests after becoming offline and send them after becoming connected again" in {
      val clientId = ClientId()
      val factory = new TestWebSocketFactory
      val newInstanceCallback = TestProbe()
      val instantiator = TestProbe()
      val connection: TestActorRef[WebSocketConnection] = TestActorRef(Props(new WebSocketConnection(newInstanceCallback.ref, instantiator.ref, clientId, factory, "15", 10, jsonProtocol)))
      val request = HistoricOperationRequest(clientId, DataStructureInstanceId(), OperationId())
      system.scheduler.scheduleOnce(0.millis) {
        connection ! OnConnect(factory.mock)
        connection ! OnClose(1)
        connection ! request
        awaitCond(factory.mock.sent.isEmpty, timeout)
        connection ! OnConnect(factory.mock)
      }
      awaitCond(factory.mock.sent.nonEmpty, timeout)
      //explicitely kill the actor or else the running job won't stop
      connection ! PoisonPill
      val sentMessages = factory.mock.sent
      sentMessages.headOption match {
        case Some(msg) => read[FormicMessage](msg.asInstanceOf[String]) should equal(HistoricOperationRequest(clientId, request.dataStructureInstanceId, request.sinceId))
        case None => fail("No message sent via WebSocket")
      }
    }

    "buffer outgoing UpdateRequests after becoming offline and send them after becoming connected again" in {
      val clientId = ClientId()
      val factory = new TestWebSocketFactory
      val newInstanceCallback = TestProbe()
      val instantiator = TestProbe()
      val connection: TestActorRef[WebSocketConnection] = TestActorRef(Props(new WebSocketConnection(newInstanceCallback.ref, instantiator.ref, clientId, factory, "16", 10, jsonProtocol)))
      val request = UpdateRequest(clientId, DataStructureInstanceId())
      system.scheduler.scheduleOnce(0.millis) {
        connection ! OnConnect(factory.mock)
        connection ! OnClose(1)
        connection ! request
        awaitCond(factory.mock.sent.isEmpty, timeout)
        connection ! OnConnect(factory.mock)
      }
      awaitCond(factory.mock.sent.nonEmpty, timeout)
      //explicitely kill the actor or else the running job won't stop
      connection ! PoisonPill
      val sentMessages = factory.mock.sent
      sentMessages.headOption match {
        case Some(msg) => read[FormicMessage](msg.asInstanceOf[String]) should equal(UpdateRequest(clientId, request.dataStructureInstanceId))
        case None => fail("No message sent via WebSocket")
      }
    }

    "buffer outgoing OperationMessages after becoming offline and send them after becoming connected again" in {
      val clientId = ClientId()
      val factory = new TestWebSocketFactory
      val newInstanceCallback = TestProbe()
      val instantiator = TestProbe()
      val connection: TestActorRef[WebSocketConnection] = TestActorRef(Props(new WebSocketConnection(newInstanceCallback.ref, instantiator.ref, clientId, factory, "17", 10, jsonProtocol)))
      val message = OperationMessage(clientId, DataStructureInstanceId(), TestClasses.dataTypeName, List(TestOperation(OperationId(), OperationContext(List.empty), clientId)))
      system.scheduler.scheduleOnce(0.millis) {
        connection ! OnConnect(factory.mock)
        connection ! OnClose(1)
        connection ! message
        awaitCond(factory.mock.sent.isEmpty, timeout)
        connection ! OnConnect(factory.mock)
      }
      awaitCond(factory.mock.sent.nonEmpty, timeout)
      //explicitely kill the actor or else the running job won't stop
      connection ! PoisonPill
      val sentMessages = factory.mock.sent
      sentMessages.headOption match {
        case Some(msg) =>
          val sentOperation = message.operations.head
          read[FormicMessage](msg.asInstanceOf[String]) should equal(
            OperationMessage(clientId, message.dataStructureInstanceId, message.dataStructure, List(TestOperation(sentOperation.id, sentOperation.operationContext, clientId)))
          )
        case None => fail("No message sent via WebSocket")
      }
    }

    "add the ClientId to OperationMessages and send them over the WebSocket" in {
      val factory = new TestWebSocketFactory
      val clientId = ClientId()
      val message = OperationMessage(null, DataStructureInstanceId(), TestClasses.dataTypeName, List(TestOperation(OperationId(), OperationContext(List.empty), clientId)))
      val newInstanceCallback = TestProbe()
      val instantiator = TestProbe()
      val connection: TestActorRef[WebSocketConnection] = TestActorRef(Props(new WebSocketConnection(newInstanceCallback.ref, instantiator.ref, clientId, factory, "11", 10, jsonProtocol)))
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
            OperationMessage(clientId, message.dataStructureInstanceId, message.dataStructure, List(TestOperation(sentOperation.id, sentOperation.operationContext, clientId)))
          )
        case None => fail("No message sent via WebSocket")
      }
    }

    "add the ClientId to CreateRequests and send them over the WebSocket" in {
      val request = CreateRequest(null, DataStructureInstanceId(), TestClasses.dataTypeName)
      val probe = TestProbe()
      val factory = new TestWebSocketFactory
      val clientId = ClientId()
      val newInstanceCallback = TestProbe()
      val instantiator = TestProbe()
      val connection: TestActorRef[WebSocketConnection] = TestActorRef(Props(new WebSocketConnection(newInstanceCallback.ref, instantiator.ref, clientId, factory, "8", 10, jsonProtocol)))
      system.scheduler.scheduleOnce(0.millis) {
        connection ! OnConnect(factory.mock)

        connection ! (probe.ref, request)
      }
      awaitCond(factory.mock.sent.nonEmpty, timeout)
      val sentMessages = factory.mock.sent
      sentMessages.headOption match {
        case Some(msg) => read[FormicMessage](msg.asInstanceOf[String]) should equal(CreateRequest(clientId, request.dataStructureInstanceId, request.dataStructure))
        case None => fail("No message sent via WebSocket")
      }
    }

    "add the ClientId to HistoricOperationRequests and send them over the WebSocket" in {
      val request = HistoricOperationRequest(null, DataStructureInstanceId(), OperationId())
      val factory = new TestWebSocketFactory
      val clientId = ClientId()
      val newInstanceCallback = TestProbe()
      val instantiator = TestProbe()
      val connection: TestActorRef[WebSocketConnection] = TestActorRef(Props(new WebSocketConnection(newInstanceCallback.ref, instantiator.ref, clientId, factory, "9", 10, jsonProtocol)))
      system.scheduler.scheduleOnce(0.millis) {
        connection ! OnConnect(factory.mock)

        connection ! request
      }
      awaitCond(factory.mock.sent.nonEmpty, timeout)
      val sentMessages = factory.mock.sent
      sentMessages.headOption match {
        case Some(msg) => read[FormicMessage](msg.asInstanceOf[String]) should equal(HistoricOperationRequest(clientId, request.dataStructureInstanceId, request.sinceId))
        case None => fail("No message sent via WebSocket")
      }
    }

    "add the ClientId to UpdateRequests and send them over the WebSocket" in {
      val request = UpdateRequest(null, DataStructureInstanceId())
      val factory = new TestWebSocketFactory
      val clientId = ClientId()
      val newInstanceCallback = TestProbe()
      val instantiator = TestProbe()
      val connection: TestActorRef[WebSocketConnection] = TestActorRef(Props(new WebSocketConnection(newInstanceCallback.ref, instantiator.ref, clientId, factory, "10", 10, jsonProtocol)))
      system.scheduler.scheduleOnce(0.millis) {
        connection ! OnConnect(factory.mock)

        connection ! request
      }
      awaitCond(factory.mock.sent.nonEmpty, timeout)
      val sentMessages = factory.mock.sent
      sentMessages.headOption match {
        case Some(msg) => read[FormicMessage](msg.asInstanceOf[String]) should equal(UpdateRequest(clientId, request.dataStructureInstanceId))
        case None => fail("No message sent via WebSocket")
      }
    }

    "send UpdateRequests for all acknowledged data type instances the dispatcher knows about when becoming online again" in {
      val clientId = ClientId()
      val factory = new TestWebSocketFactory
      val newInstanceCallback = TestProbe()
      val instantiator = TestProbe()
      val dataType1 = TestProbe()
      val dataType1Id = DataStructureInstanceId()
      val dataType2 = TestProbe()
      val dataType2Id = DataStructureInstanceId()
      val createRequest1 = CreateRequest(clientId, dataType1Id, TestClasses.dataTypeName)
      val createRequest2 = CreateRequest(clientId, dataType2Id, TestClasses.dataTypeName)
      val connection: TestActorRef[WebSocketConnection] = TestActorRef(Props(new WebSocketConnection(newInstanceCallback.ref, instantiator.ref, clientId, factory, "14", 10, jsonProtocol)))
      val probe = TestProbe()
      system.scheduler.scheduleOnce(0.millis) {
        connection ! OnConnect(factory.mock)
        //create acknowledged data type
        connection ! (dataType1.ref, createRequest1)
        connection ! CreateResponse(dataType1Id)
        connection ! OnClose(1)
        //create unacknowledged data type
        connection ! (dataType2.ref, createRequest2)
        connection ! OnConnect(factory.mock)
      }
      awaitCond(factory.mock.sent.length == 3, timeout)
      //explicitely kill the actor or else the running job won't stop
      connection ! PoisonPill
      val sentMessages = factory.mock.sent
      awaitAssert(sentMessages should contain allOf(write(createRequest1), write(createRequest2), write(UpdateRequest(clientId, dataType1Id))), 2.seconds)
    }

    "buffer only as many messages as the buffer size" in {
      val request = UpdateRequest(null, DataStructureInstanceId())
      val factory = new TestWebSocketFactory
      val clientId = ClientId()
      val newInstanceCallback = TestProbe()
      val instantiator = TestProbe()
      val connection: TestActorRef[WebSocketConnection] = TestActorRef(Props(new WebSocketConnection(newInstanceCallback.ref, instantiator.ref, clientId, factory, "15", 10, jsonProtocol)))
      val messages: ArrayBuffer[FormicMessage] = ArrayBuffer.empty
      for(x <- 0.to(15)){
        messages += UpdateRequest(ClientId(), DataStructureInstanceId())
      }
      system.scheduler.scheduleOnce(0.millis) {
        messages.foreach(msg => connection ! msg)
        connection ! OnConnect(factory.mock)
      }
      awaitCond(factory.mock.sent.nonEmpty, timeout)
      val sentMessages = factory.mock.sent
      awaitAssert(sentMessages.size should be(10), timeout)

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