package de.tu_berlin.formic.server

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestActorRef, TestKit, TestProbe}
import de.tu_berlin.formic.StopSystemAfterAll
import de.tu_berlin.formic.common.datastructure._
import de.tu_berlin.formic.common.json.FormicJsonProtocol
import de.tu_berlin.formic.common.message._
import de.tu_berlin.formic.common.{ClientId, DataStructureInstanceId, OperationId}
import de.tu_berlin.formic.server.datastructure.{TestClasses, TestDataStructureFactory, TestFormicJsonDataStructureProtocol}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.concurrent.duration._
import scala.language.postfixOps

/**
  * @author Ronny Bräunlich
  */
class UserProxySpec extends TestKit(ActorSystem("UserProxySpec"))
  with WordSpecLike
  with ImplicitSender
  with StopSystemAfterAll
  with Matchers
  with BeforeAndAfterAll {

  val jsonProtocol = FormicJsonProtocol()
  jsonProtocol.registerProtocol(new TestFormicJsonDataStructureProtocol())

  implicit val writer = jsonProtocol.writer
  implicit val reader = jsonProtocol.reader

  "User proxy" must {

    "set a ClientId to itself after instantiation" in {
      val userProxy: TestActorRef[UserProxy] = TestActorRef(Props(new UserProxy(Map.empty)))

      userProxy.underlyingActor.id shouldNot be(null)
    }


    "creates new data type instance after receiving CreateRequest, adds it to its data type set and send a CreateResponse" in {
      val factory = system.actorOf(Props[datastructure.TestDataStructureFactory])
      val userProxy: TestActorRef[UserProxy] = TestActorRef(Props(new UserProxy(Map(datastructure.TestClasses.dataStructureName -> factory))))
      val outgoingProbe = TestProbe()
      userProxy ! Connected(outgoingProbe.ref)
      val dataStructureInstanceId = DataStructureInstanceId()

      userProxy ! CreateRequest(ClientId(), dataStructureInstanceId, datastructure.TestClasses.dataStructureName)

      outgoingProbe.expectMsg(CreateResponse(dataStructureInstanceId))
      userProxy.underlyingActor.watchlist should (have size 1)
    }

    "register for changes of data type it created and forward OperationMessages" in {
      val factory = system.actorOf(Props[datastructure.TestDataStructureFactory])
      val userProxy = system.actorOf(Props(new UserProxy(Map(datastructure.TestClasses.dataStructureName -> factory))))
      val outgoingProbe = TestProbe()
      val publisherProbe = TestProbe()
      system.eventStream.subscribe(publisherProbe.ref, classOf[OperationMessage])
      userProxy ! Connected(outgoingProbe.ref)
      val dataStructureInstanceId = DataStructureInstanceId()
      userProxy ! CreateRequest(ClientId(), dataStructureInstanceId, datastructure.TestClasses.dataStructureName)
      outgoingProbe.receiveOne(3 seconds)
      val clientId: ClientId = ClientId()
      val operationMessage = OperationMessage(
        clientId,
        dataStructureInstanceId,
        datastructure.TestClasses.dataStructureName,
        List(datastructure.TestOperation(OperationId(), OperationContext(List.empty), clientId))
      )

      userProxy ! operationMessage

      //to make sure that the data type receive the message
      publisherProbe.expectMsg(operationMessage)
      outgoingProbe.expectMsg(operationMessage)
    }

    "forward OperationMessages that were applied from other users" in {
      val factory = system.actorOf(Props[datastructure.TestDataStructureFactory])
      val userProxy = system.actorOf(Props(new UserProxy(Map(datastructure.TestClasses.dataStructureName -> factory))))
      val outgoingProbe = TestProbe()
      userProxy ! Connected(outgoingProbe.ref)
      val dataStructureInstanceId = DataStructureInstanceId()
      userProxy ! CreateRequest(ClientId(), dataStructureInstanceId, datastructure.TestClasses.dataStructureName)
      outgoingProbe.receiveOne(3 seconds)
      val operationMessage = OperationMessage(
        ClientId(),
        dataStructureInstanceId,
        datastructure.TestClasses.dataStructureName,
        List(datastructure.TestOperation(OperationId(), OperationContext(List.empty), ClientId()))
      )

      system.eventStream.publish(operationMessage)

      outgoingProbe.expectMsg(operationMessage)
    }

    "ignore OperationMessages for data types it is not interested in" in {
      val factory = system.actorOf(Props[datastructure.TestDataStructureFactory])
      val userProxy = system.actorOf(Props(new UserProxy(Map(datastructure.TestClasses.dataStructureName -> factory))))
      val outgoingProbe = TestProbe()
      userProxy ! Connected(outgoingProbe.ref)
      userProxy ! CreateRequest(ClientId(), DataStructureInstanceId(), datastructure.TestClasses.dataStructureName)
      outgoingProbe.receiveOne(3 seconds)
      val operationMessage = OperationMessage(
        ClientId(),
        DataStructureInstanceId(),
        datastructure.TestClasses.dataStructureName,
        List(datastructure.TestOperation(OperationId(), OperationContext(List.empty), ClientId()))
      )

      system.eventStream.publish(operationMessage)

      outgoingProbe.expectNoMsg()
    }

    "forward HistoricOperationRequests to the correct data type" in {
      val factory = system.actorOf(Props[TestDataStructureFactory])
      val userProxy = system.actorOf(Props(new UserProxy(Map(TestClasses.dataStructureName -> factory))))
      val outgoingProbe = TestProbe()
      userProxy ! Connected(outgoingProbe.ref)
      val dataStructureInstanceId = DataStructureInstanceId()
      userProxy ! CreateRequest(ClientId(), dataStructureInstanceId, TestClasses.dataStructureName)
      outgoingProbe.receiveOne(3 seconds)
      val operationId = OperationId()
      val operationMessage = OperationMessage(
        ClientId(),
        dataStructureInstanceId,
        datastructure.TestClasses.dataStructureName,
        List(datastructure.TestOperation(operationId, OperationContext(List.empty), ClientId()))
      )
      val operationId2 = OperationId()
      val clientId2 = ClientId()
      val operationMessage2 = OperationMessage(
        clientId2,
        dataStructureInstanceId,
        datastructure.TestClasses.dataStructureName,
        List(datastructure.TestOperation(operationId2, OperationContext(List(operationId)), clientId2))
      )
      userProxy ! operationMessage
      userProxy ! operationMessage2
      outgoingProbe.receiveN(2)

      val requesterClientId = ClientId()
      userProxy ! HistoricOperationRequest(requesterClientId, dataStructureInstanceId, operationId)

      outgoingProbe.expectMsg(OperationMessage(
        requesterClientId,
        dataStructureInstanceId,
        datastructure.TestClasses.dataStructureName,
        List(datastructure.TestOperation(operationId2, OperationContext(List(operationId)), clientId2))
      ))
    }

    "forward an UpdateRequest to the correct data type and save the data type instance id" in {
      val dataStructureInstanceId = DataStructureInstanceId()
      val factory = system.actorOf(Props[TestDataStructureFactory])
      factory ! CreateRequest(ClientId(), dataStructureInstanceId, TestClasses.dataStructureName)
      Thread.sleep(500) //give the server time to create the data type
      val userProxy: TestActorRef[UserProxy] = TestActorRef(Props(new UserProxy(Map.empty)))
      val outgoingProbe = TestProbe()
      userProxy ! Connected(outgoingProbe.ref)

      userProxy ! UpdateRequest(ClientId(), dataStructureInstanceId)

      outgoingProbe.expectMsg(UpdateResponse(dataStructureInstanceId, TestClasses.dataStructureName, "{data}", Option.empty))
      userProxy.underlyingActor.watchlist should contain key dataStructureInstanceId
    }

  }
}
