package de.tu_berlin.formic.common.server.datastructure

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import de.tu_berlin.formic.StopSystemAfterAll
import de.tu_berlin.formic.common.datastructure.DataStructureName
import de.tu_berlin.formic.common.message.CreateRequest
import de.tu_berlin.formic.common.{ClientId, DataStructureInstanceId}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{Matchers, WordSpecLike}

import scala.concurrent.duration._
import scala.language.postfixOps

/**
  * @author Ronny Bräunlich
  */

class AbstractServerDataStructureFactorySpec extends TestKit(ActorSystem("AbstractServerDataStructureFactorySpec"))
  with WordSpecLike
  with ImplicitSender
  with StopSystemAfterAll
  with Matchers {

  "The factory" must {
    "create a new data type instance when receiving a CreateRequest" in {
      val factory = system.actorOf(Props[TestDataStructureFactory])
      val dataStructureInstanceId = DataStructureInstanceId()
      factory ! CreateRequest(ClientId(), dataStructureInstanceId, DataStructureName("Test"))

      val received = expectMsgClass(classOf[NewDataStructureCreated])
      received.dataStructureInstanceId should be(dataStructureInstanceId)
      received.ref shouldNot be(null)
    }

    "give a new data type instance the actor name of its datatypeinstance id" in {
      val factory = system.actorOf(Props[TestDataStructureFactory], TestClasses.dataStructureName.name)
      val dataStructureInstanceId = DataStructureInstanceId()
      factory ! CreateRequest(ClientId(), dataStructureInstanceId, DataStructureName("Test"))
      receiveN(1)

      val selection = system.actorSelection(factory.path.child(dataStructureInstanceId.id)).resolveOne(3 seconds)
      ScalaFutures.whenReady(selection) { ref =>
        ref shouldNot be(null)
      }
    }
  }


}
