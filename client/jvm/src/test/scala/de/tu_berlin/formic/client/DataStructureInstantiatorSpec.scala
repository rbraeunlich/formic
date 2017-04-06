package de.tu_berlin.formic.client

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.testkit.{EventFilter, ImplicitSender, TestActorRef, TestKit, TestProbe}
import com.typesafe.config.ConfigFactory
import de.tu_berlin.formic.client.Dispatcher.WrappedUpdateResponse
import de.tu_berlin.formic.common.datastructure.client.AbstractClientDataStructureFactory.NewDataStructureCreated
import de.tu_berlin.formic.common.{ClientId, DataStructureInstanceId, OperationId}
import de.tu_berlin.formic.common.datastructure.DataStructureName
import de.tu_berlin.formic.common.datastructure.client.AbstractClientDataStructure.ReceiveCallback
import de.tu_berlin.formic.common.message.{UpdateRequest, UpdateResponse}
import org.scalatest.{Matchers, WordSpecLike}

/**
  * @author Ronny Bräunlich
  */
class DataStructureInstantiatorSpec extends TestKit(ActorSystem("DataStructureInstantiatorSpec", ConfigFactory.parseString(
  """
  akka.loggers = ["akka.testkit.TestEventListener"]
  """)))
  with WordSpecLike
  with ImplicitSender
  with StopSystemAfterAll
  with Matchers {

  "DataTypeInstantiator" must {

    "create a new data type instance upon receiving an UpdateResponse" in {
      val clientId = ClientId()
      val testFactory = TestActorRef(Props(new TestDataStructureFactory))
      val testFactories: Map[DataStructureName, ActorRef] = Map(TestClasses.dataStructureName -> testFactory)
      val instantiator = system.actorOf(Props(new DataStructureInstantiator(testFactories, clientId)))
      val outgoingConnection = TestProbe()
      val dataTypeInstanceId = DataStructureInstanceId()
      val updateResponse = UpdateResponse(dataTypeInstanceId, TestClasses.dataStructureName, "", Option.empty)

      instantiator ! WrappedUpdateResponse(outgoingConnection.ref, updateResponse)

      val msg = expectMsgClass(classOf[NewDataStructureCreated])
      msg.dataStructureInstanceId should equal(dataTypeInstanceId)
      msg.dataStructureActor shouldNot be(null)
    }

    "create a new data type instance upon receiving an UpdateResponse with the contained data" in {
      val clientId = ClientId()
      val testFactory = TestActorRef(Props(new TestDataStructureFactory))
      val testFactories: Map[DataStructureName, ActorRef] = Map(TestClasses.dataStructureName -> testFactory)
      val instantiator = system.actorOf(Props(new DataStructureInstantiator(testFactories, clientId)))
      val outgoingConnection = TestProbe()
      val dataTypeInstanceId = DataStructureInstanceId()
      val lastOperationId = OperationId()
      val json = "[\"a\",\"b\",\"c\"]"
      val updateResponse = UpdateResponse(dataTypeInstanceId, TestClasses.dataStructureName, json, Option(lastOperationId))

      instantiator ! WrappedUpdateResponse(outgoingConnection.ref, updateResponse)

      val msg = expectMsgClass(classOf[NewDataStructureCreated])

      msg.dataStructureActor ! ReceiveCallback((_) => {})

      msg.dataStructureActor ! UpdateRequest(ClientId(), dataTypeInstanceId)
      val answer = expectMsgClass(classOf[UpdateResponse])
      answer.data should equal(json)
      answer.lastOperationId.get should equal(lastOperationId)
    }

    "throw an exception when receiving an UpdateResponse with unknown data type name" in {
      val clientId = ClientId()
      val instantiator: TestActorRef[DataStructureInstantiator] = TestActorRef(Props(new DataStructureInstantiator(Map.empty, clientId)))
      val dataTypeInstanceId = DataStructureInstanceId()
      val outgoingConnection = TestProbe()
      val updateResponse = UpdateResponse(dataTypeInstanceId, TestClasses.dataStructureName, "", Option.empty)

      EventFilter[IllegalArgumentException](occurrences = 1) intercept {
        instantiator ! WrappedUpdateResponse(outgoingConnection.ref, updateResponse)
      }
    }
  }
}
