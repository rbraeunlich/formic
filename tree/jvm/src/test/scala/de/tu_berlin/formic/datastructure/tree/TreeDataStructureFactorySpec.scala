package de.tu_berlin.formic.datastructure.tree

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import de.tu_berlin.formic.common.message.CreateRequest
import de.tu_berlin.formic.common.server.datastructure.NewDataStructureCreated
import de.tu_berlin.formic.common.{ClientId, DataStructureInstanceId}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

/**
  * @author Ronny Bräunlich
  */
class TreeDataStructureFactorySpec extends TestKit(ActorSystem("TreeDataStructureFactorySpec"))
  with WordSpecLike
  with ImplicitSender
  with Matchers
  with BeforeAndAfterAll {

  override def afterAll(): Unit = {
    super.afterAll()
    system.terminate()
  }

  "BooleanTreeDataStructureFactory" must {
    "create tree data types" in {

      val factory = system.actorOf(Props(new BooleanTreeDataStructureFactory()), "bool")
      val dataTypeInstanceId = DataStructureInstanceId()
      factory ! CreateRequest(ClientId(), dataTypeInstanceId, BooleanTreeDataStructureFactory.name)

      val response = expectMsgClass(classOf[NewDataStructureCreated])

      response.dataStructureInstanceId should be(dataTypeInstanceId)
      response.ref.path should equal(factory.path.child(dataTypeInstanceId.id))
    }
  }

  "DoubleTreeDataStructureFactory" must {
    "create tree data types" in {

      val factory = system.actorOf(Props(new DoubleTreeDataStructureFactory()), "double")
      val dataTypeInstanceId = DataStructureInstanceId()
      factory ! CreateRequest(ClientId(), dataTypeInstanceId, DoubleTreeDataStructureFactory.name)

      val response = expectMsgClass(classOf[NewDataStructureCreated])

      response.dataStructureInstanceId should be(dataTypeInstanceId)
      response.ref.path should equal(factory.path.child(dataTypeInstanceId.id))
    }
  }

  "IntegerTreeDataStructureFactory" must {
    "create tree data types" in {

      val factory = system.actorOf(Props(new IntegerTreeDataStructureFactory()), "int")
      val dataTypeInstanceId = DataStructureInstanceId()
      factory ! CreateRequest(ClientId(), dataTypeInstanceId, IntegerTreeDataStructureFactory.name)

      val response = expectMsgClass(classOf[NewDataStructureCreated])

      response.dataStructureInstanceId should be(dataTypeInstanceId)
      response.ref.path should equal(factory.path.child(dataTypeInstanceId.id))
    }
  }

  "StringDataStructureFactory" must {
    "create tree data types" in {

      val factory = system.actorOf(Props(new StringTreeDataStructureFactory()), "string")
      val dataTypeInstanceId = DataStructureInstanceId()
      factory ! CreateRequest(ClientId(), dataTypeInstanceId, StringTreeDataStructureFactory.name)

      val response = expectMsgClass(classOf[NewDataStructureCreated])

      response.dataStructureInstanceId should be(dataTypeInstanceId)
      response.ref.path should equal(factory.path.child(dataTypeInstanceId.id))
    }
  }

}
