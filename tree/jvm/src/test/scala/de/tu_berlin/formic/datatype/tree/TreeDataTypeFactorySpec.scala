package de.tu_berlin.formic.datatype.tree

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import de.tu_berlin.formic.common.message.CreateRequest
import de.tu_berlin.formic.common.server.datatype.NewDataTypeCreated
import de.tu_berlin.formic.common.{ClientId, DataStructureInstanceId}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

/**
  * @author Ronny Bräunlich
  */
class TreeDataTypeFactorySpec extends TestKit(ActorSystem("TreeDataTypeFactorySpec"))
  with WordSpecLike
  with ImplicitSender
  with Matchers
  with BeforeAndAfterAll {

  override def afterAll(): Unit = {
    super.afterAll()
    system.terminate()
  }

  "BooleanTreeDataTypeFactory" must {
    "create tree data types" in {

      val factory = system.actorOf(Props(new BooleanTreeDataTypeFactory()), "bool")
      val dataTypeInstanceId = DataStructureInstanceId()
      factory ! CreateRequest(ClientId(), dataTypeInstanceId, BooleanTreeDataTypeFactory.name)

      val response = expectMsgClass(classOf[NewDataTypeCreated])

      response.dataStructureInstanceId should be(dataTypeInstanceId)
      response.ref.path should equal(factory.path.child(dataTypeInstanceId.id))
    }
  }

  "DoubleTreeDataTypeFactory" must {
    "create tree data types" in {

      val factory = system.actorOf(Props(new DoubleTreeDataTypeFactory()), "double")
      val dataTypeInstanceId = DataStructureInstanceId()
      factory ! CreateRequest(ClientId(), dataTypeInstanceId, DoubleTreeDataTypeFactory.name)

      val response = expectMsgClass(classOf[NewDataTypeCreated])

      response.dataStructureInstanceId should be(dataTypeInstanceId)
      response.ref.path should equal(factory.path.child(dataTypeInstanceId.id))
    }
  }

  "IntegerTreeDataTypeFactory" must {
    "create tree data types" in {

      val factory = system.actorOf(Props(new IntegerTreeDataTypeFactory()), "int")
      val dataTypeInstanceId = DataStructureInstanceId()
      factory ! CreateRequest(ClientId(), dataTypeInstanceId, IntegerTreeDataTypeFactory.name)

      val response = expectMsgClass(classOf[NewDataTypeCreated])

      response.dataStructureInstanceId should be(dataTypeInstanceId)
      response.ref.path should equal(factory.path.child(dataTypeInstanceId.id))
    }
  }

  "StringDataTypeFactory" must {
    "create tree data types" in {

      val factory = system.actorOf(Props(new StringTreeDataTypeFactory()), "string")
      val dataTypeInstanceId = DataStructureInstanceId()
      factory ! CreateRequest(ClientId(), dataTypeInstanceId, StringTreeDataTypeFactory.name)

      val response = expectMsgClass(classOf[NewDataTypeCreated])

      response.dataStructureInstanceId should be(dataTypeInstanceId)
      response.ref.path should equal(factory.path.child(dataTypeInstanceId.id))
    }
  }

}
