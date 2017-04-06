package de.tu_berlin.formic.datastructure.json.client

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestActorRef, TestKit, TestProbe}
import de.tu_berlin.formic.common.datastructure.DataStructureName
import de.tu_berlin.formic.common.datastructure.client.AbstractClientDataStructureFactory.{NewDataStructureCreated, WrappedCreateRequest}
import de.tu_berlin.formic.common.message.CreateRequest
import de.tu_berlin.formic.common.{ClientId, DataStructureInstanceId}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

/**
  * @author Ronny Bräunlich
  */
class FormicJsonObjectFactorySpec extends TestKit(ActorSystem("FormicJsonFactorySpec"))
  with WordSpecLike
  with BeforeAndAfterAll
  with Matchers
  with ImplicitSender {

  override def afterAll(): Unit = {
    system.terminate()
  }

  "A FormicJsonObjectFactory" must {
    "have correct data type name" in {
      val factory: TestActorRef[FormicJsonObjectFactory] = TestActorRef(Props(new FormicJsonObjectFactory()))

      factory.underlyingActor.name should equal(DataStructureName("json"))
    }

    "create FormicJsonObjects and JsonClientDataType" in {
      val factory = system.actorOf(Props(new FormicJsonObjectFactory()))
      val outgoing = TestProbe()
      val dataTypeInstanceId = DataStructureInstanceId()
      val clientId = ClientId()

      factory ! WrappedCreateRequest(outgoing.ref, "{\"value\":1.5, \"children\": []}", Option.empty, CreateRequest(
        ClientId(), dataTypeInstanceId, FormicJsonObjectFactory.name
      ), clientId)

      val answer = expectMsgClass(classOf[NewDataStructureCreated])
      answer.dataStructureInstanceId should equal(dataTypeInstanceId)
      val wrapper = answer.wrapper
      wrapper shouldBe a[FormicJsonObject]
      wrapper.dataStructureInstanceId should equal(dataTypeInstanceId)
      wrapper.actor should equal(answer.dataStructureActor)
      wrapper.dataStructureName should equal(FormicJsonObjectFactory.name)
      wrapper.clientId should equal(clientId)
      answer.dataStructureActor should not be null
    }
  }
}
