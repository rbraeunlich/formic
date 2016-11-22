package de.tu_berlin.formic.datatype.linear.server

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import de.tu_berlin.formic.common.message.CreateRequest
import de.tu_berlin.formic.common.{ClientId, DataTypeInstanceId}
import de.tu_berlin.formic.datatype.linear.LinearDataType
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import de.tu_berlin.formic.datatype.linear.server.LinearDataTypeFactory

import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.{Failure, Success}

/**
  * @author Ronny Bräunlich
  */
class LinearDataTypeFactorySpec extends TestKit(ActorSystem("testsystem"))
  with WordSpecLike
  with ImplicitSender
  with Matchers
  with BeforeAndAfterAll {

  override def afterAll(): Unit = {
    super.afterAll()
    system.terminate()
  }

  "LinearDataTypeFactory" must {
    "create linear data types" in {
      import scala.concurrent.ExecutionContext.Implicits.global

      val factory = system.actorOf(Props(new LinearDataTypeFactory()), "testfactory")
      val dataTypeInstanceId = DataTypeInstanceId()
      factory ! CreateRequest(ClientId(), dataTypeInstanceId, LinearDataType.dataTypeName)

      system.actorSelection(factory.path.child(dataTypeInstanceId.id)).resolveOne(3 seconds).onComplete {
        case Success(ref) => //fine
        case Failure(ex) => fail(ex)
      }
    }
  }
}
