package de.tu_berlin.formic.datatype.linear.client

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestActorRef, TestKit, TestProbe}
import de.tu_berlin.formic.common.datatype.client.AbstractClientDataTypeFactory.{NewDataTypeCreated, WrappedCreateRequest}
import de.tu_berlin.formic.common.message.CreateRequest
import de.tu_berlin.formic.common.{ClientId, DataTypeInstanceId}
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
      val factory: TestActorRef[FormicBooleanListDataTypeFactory] = TestActorRef(Props(new FormicBooleanListDataTypeFactory()))

      factory.underlyingActor.name should equal(FormicBooleanListDataTypeFactory.dataTypeName)
    }

    "create FormicBooleanList and LinearClientDataType" in {
      val factory = system.actorOf(Props(new FormicBooleanListDataTypeFactory()))
      val outgoing = TestProbe()
      val dataTypeInstanceId = DataTypeInstanceId()
      val clientId = ClientId()

      factory ! WrappedCreateRequest(outgoing.ref, "[false]", Option.empty, CreateRequest(
        ClientId(), dataTypeInstanceId, FormicBooleanListDataTypeFactory.dataTypeName
      ), clientId)

      val answer = expectMsgClass(classOf[NewDataTypeCreated])
      answer.dataTypeInstanceId should equal(dataTypeInstanceId)
      val wrapper = answer.wrapper
      wrapper shouldBe a[FormicBooleanList]
      wrapper.dataTypeInstanceId should equal(dataTypeInstanceId)
      wrapper.actor should equal(answer.dataTypeActor)
      wrapper.dataTypeName should equal(FormicBooleanListDataTypeFactory.dataTypeName)
      wrapper.clientId should equal(clientId)
      answer.dataTypeActor should not be null
    }
  }

  "FormicDoubleListDataTypeFactory" must {
    "have correct data type name" in {
      val factory: TestActorRef[FormicDoubleListDataTypeFactory] = TestActorRef(Props(new FormicDoubleListDataTypeFactory()))

      factory.underlyingActor.name should equal(FormicDoubleListDataTypeFactory.dataTypeName)
    }

    "create FormicDoubleList and LinearClientDataType" in {
      val factory = system.actorOf(Props(new FormicDoubleListDataTypeFactory()))
      val outgoing = TestProbe()
      val dataTypeInstanceId = DataTypeInstanceId()
      val clientId = ClientId()

      factory ! WrappedCreateRequest(outgoing.ref, "[1.0]", Option.empty, CreateRequest(
        ClientId(), dataTypeInstanceId, FormicDoubleListDataTypeFactory.dataTypeName
      ), clientId)

      val answer = expectMsgClass(classOf[NewDataTypeCreated])
      answer.dataTypeInstanceId should equal(dataTypeInstanceId)
      val wrapper = answer.wrapper
      wrapper shouldBe a[FormicDoubleList]
      wrapper.dataTypeInstanceId should equal(dataTypeInstanceId)
      wrapper.actor should equal(answer.dataTypeActor)
      wrapper.dataTypeName should equal(FormicDoubleListDataTypeFactory.dataTypeName)
      wrapper.clientId should equal(clientId)
      answer.dataTypeActor should not be null
    }
  }

  "FormicIntegerListDataTypeFactory" must {
    "have correct data type name" in {
      val factory: TestActorRef[FormicIntegerListDataTypeFactory] = TestActorRef(Props(new FormicIntegerListDataTypeFactory()))

      factory.underlyingActor.name should equal(FormicIntegerListDataTypeFactory.dataTypeName)
    }

    "create FormicIntegerList and LinearClientDataType" in {
      val factory = system.actorOf(Props(new FormicIntegerListDataTypeFactory()))
      val outgoing = TestProbe()
      val dataTypeInstanceId = DataTypeInstanceId()
      val clientId = ClientId()

      factory ! WrappedCreateRequest(outgoing.ref, "[2]", Option.empty, CreateRequest(
        ClientId(), dataTypeInstanceId, FormicBooleanListDataTypeFactory.dataTypeName
      ), clientId)

      val answer = expectMsgClass(classOf[NewDataTypeCreated])
      answer.dataTypeInstanceId should equal(dataTypeInstanceId)
      val wrapper = answer.wrapper
      wrapper shouldBe a[FormicIntegerList]
      wrapper.dataTypeInstanceId should equal(dataTypeInstanceId)
      wrapper.actor should equal(answer.dataTypeActor)
      wrapper.dataTypeName should equal(FormicIntegerListDataTypeFactory.dataTypeName)
      wrapper.clientId should equal(clientId)
      answer.dataTypeActor should not be null
    }
  }

  "FormicStringDataTypeFactory" must {
    "have correct data type name" in {
      val factory: TestActorRef[FormicStringDataTypeFactory] = TestActorRef(Props(new FormicStringDataTypeFactory()))

      factory.underlyingActor.name should equal(FormicStringDataTypeFactory.dataTypeName)
    }

    "create FormicString and LinearClientDataType" in {
      val factory = system.actorOf(Props(new FormicStringDataTypeFactory()))
      val outgoing = TestProbe()
      val dataTypeInstanceId = DataTypeInstanceId()
      val clientId = ClientId()

      factory ! WrappedCreateRequest(outgoing.ref, "[\"b\"]", Option.empty, CreateRequest(
        ClientId(), dataTypeInstanceId, FormicStringDataTypeFactory.dataTypeName
      ), clientId)

      val answer = expectMsgClass(classOf[NewDataTypeCreated])
      answer.dataTypeInstanceId should equal(dataTypeInstanceId)
      val wrapper = answer.wrapper
      wrapper shouldBe a[FormicString]
      wrapper.dataTypeInstanceId should equal(dataTypeInstanceId)
      wrapper.actor should equal(answer.dataTypeActor)
      wrapper.dataTypeName should equal(FormicStringDataTypeFactory.dataTypeName)
      wrapper.clientId should equal(clientId)
      answer.dataTypeActor should not be null
    }
  }
}
