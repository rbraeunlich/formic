package de.tu_berlin.formic.common.server.datatype

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import de.tu_berlin.formic.StopSystemAfterAll
import de.tu_berlin.formic.common.datatype.DataStructureName
import de.tu_berlin.formic.common.message.CreateRequest
import de.tu_berlin.formic.common.{ClientId, DataStructureInstanceId$}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{Matchers, WordSpecLike}

import scala.concurrent.duration._
import scala.language.postfixOps

/**
  * @author Ronny Bräunlich
  */

class AbstractServerDataTypeFactorySpec extends TestKit(ActorSystem("AbstractServerDataTypeFactorySpec"))
  with WordSpecLike
  with ImplicitSender
  with StopSystemAfterAll
  with Matchers {

  "The factory" must {
    "create a new data type instance when receiving a CreateRequest" in {
      val factory = system.actorOf(Props[TestDataTypeFactory])
      val dataTypeInstanceId = DataStructureInstanceId()
      factory ! CreateRequest(ClientId(), dataTypeInstanceId, DataStructureName("Test"))

      val received = expectMsgClass(classOf[NewDataTypeCreated])
      received.dataTypeInstanceId should be(dataTypeInstanceId)
      received.ref shouldNot be(null)
    }

    "give a new data type instance the actor name of its datatypeinstance id" in {
      val factory = system.actorOf(Props[TestDataTypeFactory], TestClasses.dataTypeName.name)
      val dataTypeInstanceId = DataStructureInstanceId()
      factory ! CreateRequest(ClientId(), dataTypeInstanceId, DataStructureName("Test"))
      receiveN(1)

      val selection = system.actorSelection(factory.path.child(dataTypeInstanceId.id)).resolveOne(3 seconds)
      ScalaFutures.whenReady(selection) { ref =>
        ref shouldNot be(null)
      }
    }
  }


}
