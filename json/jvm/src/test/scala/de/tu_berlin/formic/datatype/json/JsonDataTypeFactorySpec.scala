package de.tu_berlin.formic.datatype.json

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import de.tu_berlin.formic.common.{ClientId, DataStructureInstanceId}
import de.tu_berlin.formic.common.message.CreateRequest
import de.tu_berlin.formic.common.server.datatype.NewDataTypeCreated
import de.tu_berlin.formic.datatype.tree.BooleanTreeDataStructureFactory
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
      val factory = system.actorOf(Props(new JsonServerDataStructureFactory()), "jsonFactory")
      val dataTypeInstanceId = DataStructureInstanceId()
      factory ! CreateRequest(ClientId(), dataTypeInstanceId, JsonServerDataStructureFactory.name)

      val response = expectMsgClass(classOf[NewDataTypeCreated])

      response.dataStructureInstanceId should be(dataTypeInstanceId)
      response.ref.path should equal(factory.path.child(dataTypeInstanceId.id))
    }
  }

}
