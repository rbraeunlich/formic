package de.tu_berlin.formic.datatype.tree.client

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestActorRef, TestKit, TestProbe}
import de.tu_berlin.formic.common.datatype.client.AbstractClientDataTypeFactory.{NewDataTypeCreated, WrappedCreateRequest}
import de.tu_berlin.formic.common.message.CreateRequest
import de.tu_berlin.formic.common.{ClientId, DataTypeInstanceId}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

/**
  * @author Ronny Bräunlich
  */
class FormicTreeDataTypeFactorySpec extends TestKit(ActorSystem("FormicTreeDataTypeFactorySpec"))
  with WordSpecLike
  with BeforeAndAfterAll
  with Matchers
  with ImplicitSender {

  override def afterAll(): Unit = {
    system.terminate()
  }

  "FormicBooleanTreeFactory" must {
    "have correct data type name" in {
      val factory: TestActorRef[FormicBooleanTreeFactory] = TestActorRef(Props(new FormicBooleanTreeFactory()))

      factory.underlyingActor.name should equal(FormicBooleanTreeFactory.name)
    }

    "create FormicBooleanTree and TreeClientDataType" in {
      val factory = system.actorOf(Props(new FormicBooleanTreeFactory()))
      val outgoing = TestProbe()
      val dataTypeInstanceId = DataTypeInstanceId()

      factory ! WrappedCreateRequest(outgoing.ref, "{\"value\":true, \"children\": []}", Option.empty, CreateRequest(
        ClientId(), dataTypeInstanceId, FormicBooleanTreeFactory.name
      ))

      val answer = expectMsgClass(classOf[NewDataTypeCreated])
      answer.dataTypeInstanceId should equal(dataTypeInstanceId)
      val wrapper = answer.wrapper
      wrapper shouldBe a[FormicBooleanTree]
      wrapper.dataTypeInstanceId should equal(dataTypeInstanceId)
      wrapper.actor should equal(answer.dataTypeActor)
      wrapper.dataTypeName should equal(FormicBooleanTreeFactory.name)
      answer.dataTypeActor should not be null
    }
  }

  "FormicDoubleTreeFactory" must {
    "have correct data type name" in {
      val factory: TestActorRef[FormicDoubleTreeFactory] = TestActorRef(Props(new FormicDoubleTreeFactory()))

      factory.underlyingActor.name should equal(FormicDoubleTreeFactory.name)
    }

    "create FormicDoubleTree and TreeClientDataType" in {
      val factory = system.actorOf(Props(new FormicDoubleTreeFactory()))
      val outgoing = TestProbe()
      val dataTypeInstanceId = DataTypeInstanceId()

      factory ! WrappedCreateRequest(outgoing.ref, "{\"value\":1.5, \"children\": []}", Option.empty, CreateRequest(
        ClientId(), dataTypeInstanceId, FormicBooleanTreeFactory.name
      ))

      val answer = expectMsgClass(classOf[NewDataTypeCreated])
      answer.dataTypeInstanceId should equal(dataTypeInstanceId)
      val wrapper = answer.wrapper
      wrapper shouldBe a[FormicDoubleTree]
      wrapper.dataTypeInstanceId should equal(dataTypeInstanceId)
      wrapper.actor should equal(answer.dataTypeActor)
      wrapper.dataTypeName should equal(FormicDoubleTreeFactory.name)
      answer.dataTypeActor should not be null
    }
  }

  "FormicIntegerTreeFactory" must {
    "have correct data type name" in {
      val factory: TestActorRef[FormicIntegerTreeFactory] = TestActorRef(Props(new FormicIntegerTreeFactory()))

      factory.underlyingActor.name should equal(FormicIntegerTreeFactory.name)
    }

    "create FormicIntegerTree and TreeClientDataType" in {
      val factory = system.actorOf(Props(new FormicIntegerTreeFactory()))
      val outgoing = TestProbe()
      val dataTypeInstanceId = DataTypeInstanceId()

      factory ! WrappedCreateRequest(outgoing.ref, "{\"value\":1, \"children\": []}", Option.empty, CreateRequest(
        ClientId(), dataTypeInstanceId, FormicBooleanTreeFactory.name
      ))

      val answer = expectMsgClass(classOf[NewDataTypeCreated])
      answer.dataTypeInstanceId should equal(dataTypeInstanceId)
      val wrapper = answer.wrapper
      wrapper shouldBe a[FormicIntegerTree]
      wrapper.dataTypeInstanceId should equal(dataTypeInstanceId)
      wrapper.actor should equal(answer.dataTypeActor)
      wrapper.dataTypeName should equal(FormicIntegerTreeFactory.name)
      answer.dataTypeActor should not be null
    }
  }

  "FormicStringTreeFactory" must {
    "have correct data type name" in {
      val factory: TestActorRef[FormicStringTreeFactory] = TestActorRef(Props(new FormicStringTreeFactory()))

      factory.underlyingActor.name should equal(FormicStringTreeFactory.name)
    }

    "create FormicStringTree and TreelientDataType" in {
      val factory = system.actorOf(Props(new FormicStringTreeFactory()))
      val outgoing = TestProbe()
      val dataTypeInstanceId = DataTypeInstanceId()

      factory ! WrappedCreateRequest(outgoing.ref, "{\"value\":\"def\", \"children\": []}", Option.empty, CreateRequest(
        ClientId(), dataTypeInstanceId, FormicBooleanTreeFactory.name
      ))

      val answer = expectMsgClass(classOf[NewDataTypeCreated])
      answer.dataTypeInstanceId should equal(dataTypeInstanceId)
      val wrapper = answer.wrapper
      wrapper shouldBe a[FormicStringTree]
      wrapper.dataTypeInstanceId should equal(dataTypeInstanceId)
      wrapper.actor should equal(answer.dataTypeActor)
      wrapper.dataTypeName should equal(FormicStringTreeFactory.name)
      answer.dataTypeActor should not be null
    }
  }


}