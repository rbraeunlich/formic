package de.tu_berlin.formic.datatype.json

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestActorRef, TestKit, TestProbe}
import de.tu_berlin.formic.common.datatype.DataTypeName
import de.tu_berlin.formic.common.datatype.client.AbstractClientDataTypeFactory.{NewDataTypeCreated, WrappedCreateRequest}
import de.tu_berlin.formic.common.message.CreateRequest
import de.tu_berlin.formic.common.{ClientId, DataTypeInstanceId}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

/**
  * @author Ronny Bräunlich
  */
class FormicJsonFactorySpec extends TestKit(ActorSystem("FormicJsonFactorySpec"))
  with WordSpecLike
  with BeforeAndAfterAll
  with Matchers
  with ImplicitSender {

  override def afterAll(): Unit = {
    system.terminate()
  }

  "A FormicJsonFactory" must {
    "have correct data type name" in {
      val factory: TestActorRef[FormicJsonFactory] = TestActorRef(Props(new FormicJsonFactory()))

      factory.underlyingActor.name should equal(DataTypeName("json"))
    }

    "create FormicJsonObjects and JsonClientDataType" in {
      val factory = system.actorOf(Props(new FormicJsonFactory()))
      val outgoing = TestProbe()
      val dataTypeInstanceId = DataTypeInstanceId()

      factory ! WrappedCreateRequest(outgoing.ref, "{\"value\":1.5, \"children\": []}", Option.empty, CreateRequest(
        ClientId(), dataTypeInstanceId, FormicJsonFactory.name
      ))

      val answer = expectMsgClass(classOf[NewDataTypeCreated])
      answer.dataTypeInstanceId should equal(dataTypeInstanceId)
      val wrapper = answer.wrapper
      wrapper shouldBe a[FormicJsonObject]
      wrapper.dataTypeInstanceId should equal(dataTypeInstanceId)
      wrapper.actor should equal(answer.dataTypeActor)
      wrapper.dataTypeName should equal(FormicJsonFactory.name)
      answer.dataTypeActor should not be null
    }
  }
}
