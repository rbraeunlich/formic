package de.tu_berlin.formic.datatype.linear.server

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import de.tu_berlin.formic.common.message.{CreateRequest, CreateResponse}
import de.tu_berlin.formic.common.server.datatype.NewDataTypeCreated
import de.tu_berlin.formic.common.{ClientId, DataTypeInstanceId}
import de.tu_berlin.formic.datatype.linear.LinearServerDataType
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.{Failure, Success}

/**
  * @author Ronny Bräunlich
  */
class LinearDataTypeFactorySpec extends TestKit(ActorSystem("LinearDataTypeFactorySpec"))
  with WordSpecLike
  with ImplicitSender
  with Matchers
  with BeforeAndAfterAll {

  override def afterAll(): Unit = {
    super.afterAll()
    system.terminate()
  }

  "BooleanListDataTypeFactory" must {
    "create linear data types" in {

      val factory = system.actorOf(Props(new BooleanListDataTypeFactory()), "bool")
      val dataTypeInstanceId = DataTypeInstanceId()
      factory ! CreateRequest(ClientId(), dataTypeInstanceId, BooleanListDataTypeFactory.name)

      val response = expectMsgClass(classOf[NewDataTypeCreated])

      response.dataTypeInstanceId should be(dataTypeInstanceId)
      response.ref.path should equal(factory.path.child(dataTypeInstanceId.id))
    }
  }

  "DoubleListDataTypeFactory" must {
    "create linear data types" in {

      val factory = system.actorOf(Props(new DoubleListDataTypeFactory()), "double")
      val dataTypeInstanceId = DataTypeInstanceId()
      factory ! CreateRequest(ClientId(), dataTypeInstanceId, DoubleListDataTypeFactory.name)

      val response = expectMsgClass(classOf[NewDataTypeCreated])

      response.dataTypeInstanceId should be(dataTypeInstanceId)
      response.ref.path should equal(factory.path.child(dataTypeInstanceId.id))
    }
  }

  "IntegerListDataTypeFactory" must {
    "create linear data types" in {

      val factory = system.actorOf(Props(new IntegerListDataTypeFactory()), "int")
      val dataTypeInstanceId = DataTypeInstanceId()
      factory ! CreateRequest(ClientId(), dataTypeInstanceId, IntegerListDataTypeFactory.name)

      val response = expectMsgClass(classOf[NewDataTypeCreated])

      response.dataTypeInstanceId should be(dataTypeInstanceId)
      response.ref.path should equal(factory.path.child(dataTypeInstanceId.id))
    }
  }

  "StringDataTypeFactory" must {
    "create linear data types" in {

      val factory = system.actorOf(Props(new BooleanListDataTypeFactory()), "string")
      val dataTypeInstanceId = DataTypeInstanceId()
      factory ! CreateRequest(ClientId(), dataTypeInstanceId, StringDataTypeFactory.name)

      val response = expectMsgClass(classOf[NewDataTypeCreated])

      response.dataTypeInstanceId should be(dataTypeInstanceId)
      response.ref.path should equal(factory.path.child(dataTypeInstanceId.id))
    }
  }
}
