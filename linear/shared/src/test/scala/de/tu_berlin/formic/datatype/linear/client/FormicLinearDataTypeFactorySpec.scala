package de.tu_berlin.formic.datatype.linear.client

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestActorRef, TestKit, TestProbe}
import de.tu_berlin.formic.common.datatype.client.AbstractClientDataStructureFactory.{NewDataTypeCreated, WrappedCreateRequest}
import de.tu_berlin.formic.common.message.CreateRequest
import de.tu_berlin.formic.common.{ClientId, DataStructureInstanceId}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

/**
  * @author Ronny Bräunlich
  */
class FormicLinearDataTypeFactorySpec extends TestKit(ActorSystem("FormicLinearDataTypeFactorySpec"))
  with WordSpecLike
  with BeforeAndAfterAll
  with Matchers
  with ImplicitSender {

  override def afterAll(): Unit = {
    system.terminate()
  }

  "FormicBooleanListDataTypeFactory" must {
    "have correct data type name" in {
      val factory: TestActorRef[FormicBooleanListDataStructureFactory] = TestActorRef(Props(new FormicBooleanListDataStructureFactory()))

      factory.underlyingActor.name should equal(FormicBooleanListDataStructureFactory.name)
    }

    "create FormicBooleanList and LinearClientDataType" in {
      val factory = system.actorOf(Props(new FormicBooleanListDataStructureFactory()))
      val outgoing = TestProbe()
      val dataTypeInstanceId = DataStructureInstanceId()
      val clientId = ClientId()

      factory ! WrappedCreateRequest(outgoing.ref, "[false]", Option.empty, CreateRequest(
        ClientId(), dataTypeInstanceId, FormicBooleanListDataStructureFactory.name
      ), clientId)

      val answer = expectMsgClass(classOf[NewDataTypeCreated])
      answer.dataTypeInstanceId should equal(dataTypeInstanceId)
      val wrapper = answer.wrapper
      wrapper shouldBe a[FormicBooleanList]
      wrapper.dataStructureInstanceId should equal(dataTypeInstanceId)
      wrapper.actor should equal(answer.dataTypeActor)
      wrapper.dataStructureName should equal(FormicBooleanListDataStructureFactory.name)
      wrapper.clientId should equal(clientId)
      answer.dataTypeActor should not be null
    }
  }

  "FormicDoubleListDataTypeFactory" must {
    "have correct data type name" in {
      val factory: TestActorRef[FormicDoubleListDataStructureFactory] = TestActorRef(Props(new FormicDoubleListDataStructureFactory()))

      factory.underlyingActor.name should equal(FormicDoubleListDataStructureFactory.name)
    }

    "create FormicDoubleList and LinearClientDataType" in {
      val factory = system.actorOf(Props(new FormicDoubleListDataStructureFactory()))
      val outgoing = TestProbe()
      val dataTypeInstanceId = DataStructureInstanceId()
      val clientId = ClientId()

      factory ! WrappedCreateRequest(outgoing.ref, "[1.0]", Option.empty, CreateRequest(
        ClientId(), dataTypeInstanceId, FormicDoubleListDataStructureFactory.name
      ), clientId)

      val answer = expectMsgClass(classOf[NewDataTypeCreated])
      answer.dataTypeInstanceId should equal(dataTypeInstanceId)
      val wrapper = answer.wrapper
      wrapper shouldBe a[FormicDoubleList]
      wrapper.dataStructureInstanceId should equal(dataTypeInstanceId)
      wrapper.actor should equal(answer.dataTypeActor)
      wrapper.dataStructureName should equal(FormicDoubleListDataStructureFactory.name)
      wrapper.clientId should equal(clientId)
      answer.dataTypeActor should not be null
    }
  }

  "FormicIntegerListDataTypeFactory" must {
    "have correct data type name" in {
      val factory: TestActorRef[FormicIntegerListDataStructureFactory] = TestActorRef(Props(new FormicIntegerListDataStructureFactory()))

      factory.underlyingActor.name should equal(FormicIntegerListDataStructureFactory.name)
    }

    "create FormicIntegerList and LinearClientDataType" in {
      val factory = system.actorOf(Props(new FormicIntegerListDataStructureFactory()))
      val outgoing = TestProbe()
      val dataTypeInstanceId = DataStructureInstanceId()
      val clientId = ClientId()

      factory ! WrappedCreateRequest(outgoing.ref, "[2]", Option.empty, CreateRequest(
        ClientId(), dataTypeInstanceId, FormicBooleanListDataStructureFactory.name
      ), clientId)

      val answer = expectMsgClass(classOf[NewDataTypeCreated])
      answer.dataTypeInstanceId should equal(dataTypeInstanceId)
      val wrapper = answer.wrapper
      wrapper shouldBe a[FormicIntegerList]
      wrapper.dataStructureInstanceId should equal(dataTypeInstanceId)
      wrapper.actor should equal(answer.dataTypeActor)
      wrapper.dataStructureName should equal(FormicIntegerListDataStructureFactory.name)
      wrapper.clientId should equal(clientId)
      answer.dataTypeActor should not be null
    }
  }

  "FormicStringDataTypeFactory" must {
    "have correct data type name" in {
      val factory: TestActorRef[FormicStringDataStructureFactory] = TestActorRef(Props(new FormicStringDataStructureFactory()))

      factory.underlyingActor.name should equal(FormicStringDataStructureFactory.name)
    }

    "create FormicString and LinearClientDataType" in {
      val factory = system.actorOf(Props(new FormicStringDataStructureFactory()))
      val outgoing = TestProbe()
      val dataTypeInstanceId = DataStructureInstanceId()
      val clientId = ClientId()

      factory ! WrappedCreateRequest(outgoing.ref, "[\"b\"]", Option.empty, CreateRequest(
        ClientId(), dataTypeInstanceId, FormicStringDataStructureFactory.name
      ), clientId)

      val answer = expectMsgClass(classOf[NewDataTypeCreated])
      answer.dataTypeInstanceId should equal(dataTypeInstanceId)
      val wrapper = answer.wrapper
      wrapper shouldBe a[FormicString]
      wrapper.dataStructureInstanceId should equal(dataTypeInstanceId)
      wrapper.actor should equal(answer.dataTypeActor)
      wrapper.dataStructureName should equal(FormicStringDataStructureFactory.name)
      wrapper.clientId should equal(clientId)
      answer.dataTypeActor should not be null
    }
  }
}
