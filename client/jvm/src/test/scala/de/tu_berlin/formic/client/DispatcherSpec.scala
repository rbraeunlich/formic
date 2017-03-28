package de.tu_berlin.formic.client

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.testkit.{EventFilter, ImplicitSender, TestActorRef, TestKit, TestProbe}
import com.typesafe.config.ConfigFactory
import de.tu_berlin.formic.client.Dispatcher.{ErrorMessage, KnownDataTypeIds, RequestKnownDataTypeIds, WrappedUpdateResponse}
import de.tu_berlin.formic.common.datatype.DataTypeName
import de.tu_berlin.formic.common.datatype.client.AbstractClientDataTypeFactory.{NewDataTypeCreated, WrappedCreateRequest}
import de.tu_berlin.formic.common.message._
import de.tu_berlin.formic.common.{ClientId, DataStructureInstanceId$, OperationId}
import org.scalatest.{Matchers, WordSpecLike}

/**
  * @author Ronny Bräunlich
  */
class DispatcherSpec extends TestKit(ActorSystem("DispatcherSpec", ConfigFactory.parseString(
  """
  akka.loggers = ["akka.testkit.TestEventListener"]
  """)))
  with WordSpecLike
  with ImplicitSender
  with StopSystemAfterAll
  with Matchers {

  "Dispatcher" must {
    "log errors from the WebSocket connection" in {
      val errorText = "Error from WebSocket connection: Test error"
      val dispatcher = system.actorOf(Props(new Dispatcher(null, TestProbe().ref, TestProbe().ref)))

      EventFilter.error(message = errorText, occurrences = 1) intercept {
        dispatcher ! ErrorMessage("Test error")
      }
    }

    "create a new data type instance and remember it when receiving an UpdateResponse" in {
      val testFactory = TestActorRef(Props(new TestDataTypeFactory))
      val testFactories: Map[DataTypeName, ActorRef] = Map(TestClasses.dataTypeName -> testFactory)
      val instantiator = TestActorRef(Props(new DataTypeInstantiator(testFactories, ClientId())))
      val newInstanceCallback = TestProbe()

      val dispatcher: TestActorRef[Dispatcher] = TestActorRef(Props(new Dispatcher(null, newInstanceCallback.ref, instantiator)))
      val dataTypeInstanceId = DataStructureInstanceId()

      dispatcher ! UpdateResponse(dataTypeInstanceId, TestClasses.dataTypeName, "a", Option.empty)

      dispatcher.underlyingActor.instances should contain key dataTypeInstanceId
      newInstanceCallback.expectMsgClass(classOf[NewDataTypeCreated])
    }

    "forward an operation message to the correct data type instance" in {
      val clientId = ClientId()
      val testDataType = TestProbe()
      val testDataType2 = TestProbe()
      val testFactory = TestProbe()
      val testFactories: Map[DataTypeName, ActorRef] = Map(TestClasses.dataTypeName -> testFactory.ref)
      val instantiator = TestActorRef(Props(new DataTypeInstantiator(testFactories, clientId)))
      val dispatcher: TestActorRef[Dispatcher] = TestActorRef(Props(new Dispatcher(null, TestProbe().ref, instantiator)))
      val dataTypeInstanceId = DataStructureInstanceId()
      val dataTypeInstanceId2 = DataStructureInstanceId()
      //create two data types
      dispatcher ! UpdateResponse(dataTypeInstanceId, TestClasses.dataTypeName, "a", Option.empty)
      testFactory.expectMsg(WrappedCreateRequest(null, "a", Option.empty,CreateRequest(null, dataTypeInstanceId, TestClasses.dataTypeName), clientId))
      testFactory.reply(NewDataTypeCreated(dataTypeInstanceId, testDataType.ref, new TestFormicDataType))
      dispatcher ! UpdateResponse(dataTypeInstanceId2, TestClasses.dataTypeName, "a", Option.empty)
      testFactory.expectMsg(WrappedCreateRequest(null, "a", Option.empty, CreateRequest(null, dataTypeInstanceId2, TestClasses.dataTypeName), clientId))
      testFactory.reply(NewDataTypeCreated(dataTypeInstanceId2, testDataType2.ref, new TestFormicDataType))

      val opMessage = OperationMessage(ClientId(), dataTypeInstanceId, TestClasses.dataTypeName, List.empty)
      dispatcher ! opMessage

      testDataType.expectMsg(opMessage)
    }

    "log a warning if it does not know the data type instance of an operation message" in {
      val message = OperationMessage(ClientId(), DataStructureInstanceId(), TestClasses.dataTypeName, List.empty)
      val warningText = s"Did not find data type instance with id ${message.dataTypeInstanceId}, dropping message $message"
      val dispatcher = system.actorOf(Props(new Dispatcher(null, TestProbe().ref, TestProbe().ref)))

      EventFilter.warning(message = warningText, occurrences = 1) intercept {
        dispatcher ! message
      }
    }

    "remember the actor when receiving a tuple of actor and CreateRequest" in {
      val instantiator = TestActorRef(Props(new DataTypeInstantiator(Map.empty, ClientId())))
      val dataTypeInstanceId = DataStructureInstanceId()
      val request = CreateRequest(ClientId(), dataTypeInstanceId, TestClasses.dataTypeName)
      val actor = TestProbe()
      val dispatcher: TestActorRef[Dispatcher] = TestActorRef(Props(new Dispatcher(null, TestProbe().ref, instantiator)))

      dispatcher ! (actor.ref, request)

      dispatcher.underlyingActor.instances should contain (dataTypeInstanceId -> actor.ref)
    }

    "forward an HistoricOperationRequest to the outgoing connection" in {
      val instantiator = TestActorRef(Props(new DataTypeInstantiator(Map.empty, ClientId())))
      val outgoing = TestProbe()
      val dispatcher: TestActorRef[Dispatcher] = TestActorRef(Props(new Dispatcher(outgoing.ref, TestProbe().ref, instantiator)))
      val historicRequest = HistoricOperationRequest(ClientId(), DataStructureInstanceId(), OperationId())

      dispatcher ! historicRequest

      outgoing.expectMsg(historicRequest)
    }

    "forward an CreateResponse to the correct data type instance" in {
      val testDataType = TestProbe()
      val testDataType2 = TestProbe()
      val testFactory = TestProbe()
      val clientId = ClientId()
      val testFactories: Map[DataTypeName, ActorRef] = Map(TestClasses.dataTypeName -> testFactory.ref)
      val instantiator = TestActorRef(Props(new DataTypeInstantiator(testFactories, clientId)))
      val dispatcher: TestActorRef[Dispatcher] = TestActorRef(Props(new Dispatcher(null, TestProbe().ref, instantiator)))
      val dataTypeInstanceId = DataStructureInstanceId()
      val dataTypeInstanceId2 = DataStructureInstanceId()
      //create two data types
      dispatcher ! UpdateResponse(dataTypeInstanceId, TestClasses.dataTypeName, "a", Option.empty)
      testFactory.expectMsg(WrappedCreateRequest(null, "a", Option.empty,CreateRequest(null, dataTypeInstanceId, TestClasses.dataTypeName), clientId))
      testFactory.reply(NewDataTypeCreated(dataTypeInstanceId, testDataType.ref, new TestFormicDataType))
      dispatcher ! UpdateResponse(dataTypeInstanceId2, TestClasses.dataTypeName, "a", Option.empty)
      testFactory.expectMsg(WrappedCreateRequest(null, "a", Option.empty, CreateRequest(null, dataTypeInstanceId2, TestClasses.dataTypeName), clientId))
      testFactory.reply(NewDataTypeCreated(dataTypeInstanceId2, testDataType2.ref, new TestFormicDataType))
      val response = CreateResponse(dataTypeInstanceId2)

      dispatcher ! response

      testDataType2.expectMsg(response)
    }

    "ignore UpdateResponses for data types it already knows about" in {
      val testFactory: TestActorRef[TestDataTypeFactory] = TestActorRef(Props(new TestDataTypeFactory))
      val instantiator = new TestProbe(system)
      val newInstanceCallback = TestProbe()

      val dispatcher: TestActorRef[Dispatcher] = TestActorRef(Props(new Dispatcher(null, newInstanceCallback.ref, instantiator.ref)))
      val dataTypeInstanceId = DataStructureInstanceId()

      dispatcher ! UpdateResponse(dataTypeInstanceId, TestClasses.dataTypeName, "a", Option.empty)
      instantiator.expectMsgPF(){
        case WrappedUpdateResponse(outgoing, rep) =>
          instantiator.forward(testFactory, WrappedCreateRequest(outgoing, rep.data, rep.lastOperationId, CreateRequest(null, rep.dataTypeInstanceId, TestClasses.dataTypeName), ClientId()))
      }
      dispatcher.underlyingActor.instances should contain key dataTypeInstanceId

      dispatcher ! UpdateResponse(dataTypeInstanceId, TestClasses.dataTypeName, "a", Option.empty)
      instantiator.expectNoMsg()
    }

    "answer the RequestKnownDataTypeIds message" in {
      val testFactory: TestActorRef[TestDataTypeFactory] = TestActorRef(Props(new TestDataTypeFactory))
      val instantiator = new TestProbe(system){
        def answerWrappedUpdateResponse() = {
          expectMsgPF(){
            case WrappedUpdateResponse(outgoing, rep) =>
            forward(testFactory, WrappedCreateRequest(outgoing, rep.data, rep.lastOperationId, CreateRequest(null, rep.dataTypeInstanceId, TestClasses.dataTypeName), ClientId()))
          }
        }
      }
      val newInstanceCallback = TestProbe()

      val dispatcher: TestActorRef[Dispatcher] = TestActorRef(Props(new Dispatcher(null, newInstanceCallback.ref, instantiator.ref)))
      val dataTypeInstanceId = DataStructureInstanceId()
      val dataTypeInstanceId2 = DataStructureInstanceId()

      dispatcher ! UpdateResponse(dataTypeInstanceId, TestClasses.dataTypeName, "a", Option.empty)
      instantiator.answerWrappedUpdateResponse()
      dispatcher ! UpdateResponse(dataTypeInstanceId2, TestClasses.dataTypeName, "a", Option.empty)
      instantiator.answerWrappedUpdateResponse()

      dispatcher ! RequestKnownDataTypeIds

      expectMsg(KnownDataTypeIds(Set(dataTypeInstanceId, dataTypeInstanceId2)))
    }
  }
}

