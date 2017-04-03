package de.tu_berlin.formic.datastructure.linear.server

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import de.tu_berlin.formic.common.message.{CreateRequest, CreateResponse}
import de.tu_berlin.formic.common.server.datastructure.NewDataStructureCreated
import de.tu_berlin.formic.common.{ClientId, DataStructureInstanceId}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.{Failure, Success}

/**
  * @author Ronny Bräunlich
  */
class LinearDataStructureFactorySpec extends TestKit(ActorSystem("LinearDataStructureFactorySpec"))
  with WordSpecLike
  with ImplicitSender
  with Matchers
  with BeforeAndAfterAll {

  override def afterAll(): Unit = {
    super.afterAll()
    system.terminate()
  }

  "BooleanListDataStructureFactory" must {
    "create linear data types" in {

      val factory = system.actorOf(Props(new BooleanListDataStructureFactory()), "bool")
      val dataTypeInstanceId = DataStructureInstanceId()
      factory ! CreateRequest(ClientId(), dataTypeInstanceId, BooleanListDataStructureFactory.name)

      val response = expectMsgClass(classOf[NewDataStructureCreated])

      response.dataStructureInstanceId should be(dataTypeInstanceId)
      response.ref.path should equal(factory.path.child(dataTypeInstanceId.id))
    }
  }

  "DoubleListDataStructureFactory" must {
    "create linear data types" in {

      val factory = system.actorOf(Props(new DoubleListDataStructureFactory()), "double")
      val dataTypeInstanceId = DataStructureInstanceId()
      factory ! CreateRequest(ClientId(), dataTypeInstanceId, DoubleListDataStructureFactory.name)

      val response = expectMsgClass(classOf[NewDataStructureCreated])

      response.dataStructureInstanceId should be(dataTypeInstanceId)
      response.ref.path should equal(factory.path.child(dataTypeInstanceId.id))
    }
  }

  "IntegerListDataStructureFactory" must {
    "create linear data types" in {

      val factory = system.actorOf(Props(new IntegerListDataStructureFactory()), "int")
      val dataTypeInstanceId = DataStructureInstanceId()
      factory ! CreateRequest(ClientId(), dataTypeInstanceId, IntegerListDataStructureFactory.name)

      val response = expectMsgClass(classOf[NewDataStructureCreated])

      response.dataStructureInstanceId should be(dataTypeInstanceId)
      response.ref.path should equal(factory.path.child(dataTypeInstanceId.id))
    }
  }

  "StringDataStructureFactory" must {
    "create linear data types" in {

      val factory = system.actorOf(Props(new StringDataStructureFactory()), "string")
      val dataTypeInstanceId = DataStructureInstanceId()
      factory ! CreateRequest(ClientId(), dataTypeInstanceId, StringDataStructureFactory.name)

      val response = expectMsgClass(classOf[NewDataStructureCreated])

      response.dataStructureInstanceId should be(dataTypeInstanceId)
      response.ref.path should equal(factory.path.child(dataTypeInstanceId.id))
    }
  }
}
