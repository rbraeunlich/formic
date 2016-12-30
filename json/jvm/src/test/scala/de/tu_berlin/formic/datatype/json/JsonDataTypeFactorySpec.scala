package de.tu_berlin.formic.datatype.json

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import de.tu_berlin.formic.common.{ClientId, DataTypeInstanceId}
import de.tu_berlin.formic.common.message.CreateRequest
import de.tu_berlin.formic.common.server.datatype.NewDataTypeCreated
import de.tu_berlin.formic.datatype.tree.BooleanTreeDataTypeFactory
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

/**
  * @author Ronny Bräunlich
  */
class JsonDataTypeFactorySpec extends TestKit(ActorSystem("JsonDataTypeFactorySpec"))
  with WordSpecLike
  with ImplicitSender
  with Matchers
  with BeforeAndAfterAll {

  override def afterAll(): Unit = {
    super.afterAll()
    system.terminate()
  }

  "A JsonDataTypeFactory" must {
    "create Json data types" in {
      val factory = system.actorOf(Props(new JsonDataTypeFactory()), "jsonFactory")
      val dataTypeInstanceId = DataTypeInstanceId()
      factory ! CreateRequest(ClientId(), dataTypeInstanceId, JsonDataTypeFactory.name)

      val response = expectMsgClass(classOf[NewDataTypeCreated])

      response.dataTypeInstanceId should be(dataTypeInstanceId)
      response.ref.path should equal(factory.path.child(dataTypeInstanceId.id))
    }
  }

}
