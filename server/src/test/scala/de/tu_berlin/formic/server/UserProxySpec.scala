package de.tu_berlin.formic.server

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestActorRef, TestKit, TestProbe}
import de.tu_berlin.formic.StopSystemAfterAll
import de.tu_berlin.formic.common.datatype._
import de.tu_berlin.formic.common.json.FormicJsonProtocol
import de.tu_berlin.formic.common.json.FormicJsonProtocol._
import de.tu_berlin.formic.common.message._
import de.tu_berlin.formic.common.{ClientId, DataTypeInstanceId, OperationId}
import de.tu_berlin.formic.server.datatype._
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import upickle.default._

import scala.concurrent.duration._

/**
  * @author Ronny Bräunlich
  */
class UserProxySpec extends TestKit(ActorSystem("testsystem"))
  with WordSpecLike
  with ImplicitSender
  with StopSystemAfterAll
  with Matchers
  with BeforeAndAfterAll {

  override def beforeAll(): Unit = {
    FormicJsonProtocol.registerProtocol(new TestFormicJsonDataTypeProtocol())
  }

  override def afterAll(): Unit = {
    FormicJsonProtocol.clear()
  }

  "User proxy" must {

    "set a ClientId to itself after instantiation" in {
      val userProxy: TestActorRef[UserProxy] = TestActorRef(Props(new UserProxy(Map.empty)))

      userProxy.underlyingActor.id shouldNot be(null)
    }


    "creates new data type instance after receiving CreateRequest, adds it to its data type set and send a CreateResponse" in {
      val factory = system.actorOf(Props[TestDataTypeFactory])
      val userProxy: TestActorRef[UserProxy] = TestActorRef(Props(new UserProxy(Map(TestClasses.dataTypeName -> factory))))
      val outgoingProbe = TestProbe()
      userProxy ! Connected(outgoingProbe.ref)
      val dataTypeInstanceId = DataTypeInstanceId()

      userProxy ! IncomingMessage(write(CreateRequest(ClientId(), dataTypeInstanceId, TestClasses.dataTypeName)))

      outgoingProbe.expectMsg(OutgoingMessage(write(CreateResponse(dataTypeInstanceId))))
      userProxy.underlyingActor.watchlist should (have size 1)
    }

    "register for changes of data type it created and forward OperationMessages" in {
      val factory = system.actorOf(Props[TestDataTypeFactory])
      val userProxy = system.actorOf(Props(new UserProxy(Map(TestClasses.dataTypeName -> factory))))
      val outgoingProbe = TestProbe()
      val publisherProbe = TestProbe()
      system.eventStream.subscribe(publisherProbe.ref, classOf[OperationMessage])
      userProxy ! Connected(outgoingProbe.ref)
      val dataTypeInstanceId = DataTypeInstanceId()
      userProxy ! IncomingMessage(write(CreateRequest(ClientId(), dataTypeInstanceId, TestClasses.dataTypeName)))
      outgoingProbe.receiveOne(3 seconds)
      val operationMessage = OperationMessage(
        ClientId(),
        dataTypeInstanceId,
        TestClasses.dataTypeName,
        List(TestOperation(OperationId(), OperationContext(List.empty), ClientId()))
      )

      userProxy ! IncomingMessage(write(operationMessage))

      //to make sure that the data type receive the message
      publisherProbe.expectMsg(operationMessage)
      outgoingProbe.expectMsg(OutgoingMessage(write(operationMessage)))
    }

    "forward OperationMessages that were applied from other users" in {
      val factory = system.actorOf(Props[TestDataTypeFactory])
      val userProxy = system.actorOf(Props(new UserProxy(Map(TestClasses.dataTypeName -> factory))))
      val outgoingProbe = TestProbe()
      userProxy ! Connected(outgoingProbe.ref)
      val dataTypeInstanceId = DataTypeInstanceId()
      userProxy ! IncomingMessage(write(CreateRequest(ClientId(), dataTypeInstanceId, TestClasses.dataTypeName)))
      outgoingProbe.receiveOne(3 seconds)
      val operationMessage = OperationMessage(
        ClientId(),
        dataTypeInstanceId,
        TestClasses.dataTypeName,
        List(TestOperation(OperationId(), OperationContext(List.empty), ClientId()))
      )

      system.eventStream.publish(operationMessage)

      outgoingProbe.expectMsg(OutgoingMessage(write(operationMessage)))
    }

    "ignore OperationMessages for data types it is not interested in" in {
      val factory = system.actorOf(Props[TestDataTypeFactory])
      val userProxy = system.actorOf(Props(new UserProxy(Map(TestClasses.dataTypeName -> factory))))
      val outgoingProbe = TestProbe()
      userProxy ! Connected(outgoingProbe.ref)
      userProxy ! IncomingMessage(write(CreateRequest(ClientId(), DataTypeInstanceId(), TestClasses.dataTypeName)))
      outgoingProbe.receiveOne(3 seconds)
      val operationMessage = OperationMessage(
        ClientId(),
        DataTypeInstanceId(),
        TestClasses.dataTypeName,
        List(TestOperation(OperationId(), OperationContext(List.empty), ClientId()))
      )

      system.eventStream.publish(operationMessage)

      outgoingProbe.expectNoMsg()
    }

    "forward HistoricOperationRequests to the correct data type" in {
      val factory = system.actorOf(Props[TestDataTypeFactory])
      val userProxy = system.actorOf(Props(new UserProxy(Map(TestClasses.dataTypeName -> factory))))
      val outgoingProbe = TestProbe()
      userProxy ! Connected(outgoingProbe.ref)
      val dataTypeInstanceId = DataTypeInstanceId()
      userProxy ! IncomingMessage(write(CreateRequest(ClientId(), dataTypeInstanceId, TestClasses.dataTypeName)))
      outgoingProbe.receiveOne(3 seconds)
      val operationId = OperationId()
      val operationMessage = OperationMessage(
        ClientId(),
        dataTypeInstanceId,
        TestClasses.dataTypeName,
        List(TestOperation(operationId, OperationContext(List.empty), ClientId()))
      )
      val operationId2 = OperationId()
      val clientId2 = ClientId()
      val operationMessage2 = OperationMessage(
        clientId2,
        dataTypeInstanceId,
        TestClasses.dataTypeName,
        List(TestOperation(operationId2, OperationContext(List(operationId)), clientId2))
      )
      userProxy ! IncomingMessage(write(operationMessage))
      userProxy ! IncomingMessage(write(operationMessage2))
      outgoingProbe.receiveN(2)

      val requesterClientId = ClientId()
      userProxy ! IncomingMessage(write(HistoricOperationRequest(requesterClientId, dataTypeInstanceId, operationId)))

      outgoingProbe.expectMsg(OutgoingMessage(write(OperationMessage(
        requesterClientId,
        dataTypeInstanceId,
        TestClasses.dataTypeName,
        List(TestOperation(operationId2, OperationContext(List(operationId)), clientId2))
      ))))
    }

    "forward an UpdateRequest to the correct data type" in {
      val dataTypeInstanceId = DataTypeInstanceId()
      val factory = system.actorOf(Props[TestDataTypeFactory])
      factory ! CreateRequest(ClientId(), dataTypeInstanceId, TestClasses.dataTypeName)
      val userProxy = system.actorOf(Props(new UserProxy(Map.empty)))
      val outgoingProbe = TestProbe()
      userProxy ! Connected(outgoingProbe.ref)

      userProxy ! IncomingMessage(write(UpdateRequest(ClientId(), dataTypeInstanceId)))

      outgoingProbe.expectMsg(OutgoingMessage(write(UpdateResponse(dataTypeInstanceId, TestClasses.dataTypeName, "{data}"))))
    }
  }
}