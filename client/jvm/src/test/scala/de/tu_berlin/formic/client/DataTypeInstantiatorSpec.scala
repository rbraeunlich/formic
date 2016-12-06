package de.tu_berlin.formic.client

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.testkit.{EventFilter, ImplicitSender, TestActorRef, TestKit, TestProbe}
import com.typesafe.config.ConfigFactory
import de.tu_berlin.formic.client.Dispatcher.WrappedUpdateResponse
import de.tu_berlin.formic.common.datatype.client.AbstractClientDataTypeFactory.NewDataTypeCreated
import de.tu_berlin.formic.common.{ClientId, DataTypeInstanceId}
import de.tu_berlin.formic.common.datatype.DataTypeName
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
      val testFactory = TestActorRef(Props(new TestDataTypeFactory))
      val testFactories: Map[DataTypeName, ActorRef] = Map(TestClasses.dataTypeName -> testFactory)
      val instantiator = system.actorOf(Props(new DataTypeInstantiator(testFactories)))
      val outgoingConnection = TestProbe()
      val dataTypeInstanceId = DataTypeInstanceId()
      val updateResponse = UpdateResponse(dataTypeInstanceId, TestClasses.dataTypeName, "")

      instantiator ! WrappedUpdateResponse(outgoingConnection.ref, updateResponse)

      val msg = expectMsgClass(classOf[NewDataTypeCreated])
      msg.dataTypeInstanceId should equal(dataTypeInstanceId)
      msg.dataTypeActor shouldNot be(null)
    }

    "create a new data type instance upon receiving an UpdateResponse with the contained data" in {
      val testFactory = TestActorRef(Props(new TestDataTypeFactory))
      val testFactories: Map[DataTypeName, ActorRef] = Map(TestClasses.dataTypeName -> testFactory)
      val instantiator = system.actorOf(Props(new DataTypeInstantiator(testFactories)))
      val outgoingConnection = TestProbe()
      val dataTypeInstanceId = DataTypeInstanceId()
      val json = "[\"a\",\"b\",\"c\"]"
      val updateResponse = UpdateResponse(dataTypeInstanceId, TestClasses.dataTypeName, json)

      instantiator ! WrappedUpdateResponse(outgoingConnection.ref, updateResponse)

      val msg = expectMsgClass(classOf[NewDataTypeCreated])

      msg.dataTypeActor ! ReceiveCallback(() => {})

      msg.dataTypeActor ! UpdateRequest(ClientId(), dataTypeInstanceId)
      val answer = expectMsgClass(classOf[UpdateResponse])
      answer.data should equal(json)
    }

    "throw an exception when receiving an UpdateResponse with unknown data type name" in {
      val instantiator: TestActorRef[DataTypeInstantiator] = TestActorRef(Props(new DataTypeInstantiator(Map.empty)))
      val dataTypeInstanceId = DataTypeInstanceId()
      val outgoingConnection = TestProbe()
      val updateResponse = UpdateResponse(dataTypeInstanceId, TestClasses.dataTypeName, "")

      EventFilter[IllegalArgumentException](occurrences = 1) intercept {
        instantiator ! WrappedUpdateResponse(outgoingConnection.ref, updateResponse)
      }
    }
  }
}
