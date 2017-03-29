package de.tu_berlin.formic.client

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.testkit.{EventFilter, ImplicitSender, TestActorRef, TestKit, TestProbe}
import com.typesafe.config.ConfigFactory
import de.tu_berlin.formic.client.Dispatcher.WrappedUpdateResponse
import de.tu_berlin.formic.common.datatype.client.AbstractClientDataTypeFactory.NewDataTypeCreated
import de.tu_berlin.formic.common.{ClientId, DataStructureInstanceId, OperationId}
import de.tu_berlin.formic.common.datatype.DataStructureName
import de.tu_berlin.formic.common.datatype.client.AbstractClientDataType.ReceiveCallback
import de.tu_berlin.formic.common.message.{UpdateRequest, UpdateResponse}
import org.scalatest.{Matchers, WordSpecLike}

/**
  * @author Ronny Bräunlich
  */
class DataTypeInstantiatorSpec extends TestKit(ActorSystem("DataTypeInstantiatorSpec", ConfigFactory.parseString(
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
      val testFactory = TestActorRef(Props(new TestDataTypeFactory))
      val testFactories: Map[DataStructureName, ActorRef] = Map(TestClasses.dataTypeName -> testFactory)
      val instantiator = system.actorOf(Props(new DataTypeInstantiator(testFactories, clientId)))
      val outgoingConnection = TestProbe()
      val dataTypeInstanceId = DataStructureInstanceId()
      val updateResponse = UpdateResponse(dataTypeInstanceId, TestClasses.dataTypeName, "", Option.empty)

      instantiator ! WrappedUpdateResponse(outgoingConnection.ref, updateResponse)

      val msg = expectMsgClass(classOf[NewDataTypeCreated])
      msg.dataTypeInstanceId should equal(dataTypeInstanceId)
      msg.dataTypeActor shouldNot be(null)
    }

    "create a new data type instance upon receiving an UpdateResponse with the contained data" in {
      val clientId = ClientId()
      val testFactory = TestActorRef(Props(new TestDataTypeFactory))
      val testFactories: Map[DataStructureName, ActorRef] = Map(TestClasses.dataTypeName -> testFactory)
      val instantiator = system.actorOf(Props(new DataTypeInstantiator(testFactories, clientId)))
      val outgoingConnection = TestProbe()
      val dataTypeInstanceId = DataStructureInstanceId()
      val lastOperationId = OperationId()
      val json = "[\"a\",\"b\",\"c\"]"
      val updateResponse = UpdateResponse(dataTypeInstanceId, TestClasses.dataTypeName, json, Option(lastOperationId))

      instantiator ! WrappedUpdateResponse(outgoingConnection.ref, updateResponse)

      val msg = expectMsgClass(classOf[NewDataTypeCreated])

      msg.dataTypeActor ! ReceiveCallback((_) => {})

      msg.dataTypeActor ! UpdateRequest(ClientId(), dataTypeInstanceId)
      val answer = expectMsgClass(classOf[UpdateResponse])
      answer.data should equal(json)
      answer.lastOperationId.get should equal(lastOperationId)
    }

    "throw an exception when receiving an UpdateResponse with unknown data type name" in {
      val clientId = ClientId()
      val instantiator: TestActorRef[DataTypeInstantiator] = TestActorRef(Props(new DataTypeInstantiator(Map.empty, clientId)))
      val dataTypeInstanceId = DataStructureInstanceId()
      val outgoingConnection = TestProbe()
      val updateResponse = UpdateResponse(dataTypeInstanceId, TestClasses.dataTypeName, "", Option.empty)

      EventFilter[IllegalArgumentException](occurrences = 1) intercept {
        instantiator ! WrappedUpdateResponse(outgoingConnection.ref, updateResponse)
      }
    }
  }
}
