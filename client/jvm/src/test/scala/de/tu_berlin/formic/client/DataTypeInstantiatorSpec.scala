package de.tu_berlin.formic.client

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.testkit.{EventFilter, ImplicitSender, TestActorRef, TestKit}
import com.typesafe.config.ConfigFactory
import de.tu_berlin.formic.StopSystemAfterAll
import de.tu_berlin.formic.common.DataTypeInstanceId
import de.tu_berlin.formic.common.datatype.DataTypeName
import de.tu_berlin.formic.common.message.UpdateResponse
import de.tu_berlin.formic.common.server.datatype._
import org.scalatest.{Matchers, WordSpecLike}

/**
  * @author Ronny Bräunlich
  */
class DataTypeInstantiatorSpec extends TestKit(ActorSystem("DataTypeInstantiatorSpec", ConfigFactory.parseString(
  """
  akka.loggers = ["akka.testkit.TestEventListener"]
  """)))
  with WordSpecLike
  with ImplicitSender
  with StopSystemAfterAll
  with Matchers {

  "DataTypeInstantiator" must {
    "create a new data type instance upon receiving an UpdateResponse" in {
      val testFactory = TestActorRef(Props(new TestDataTypeFactory))
      val testFactories: Map[DataTypeName, ActorRef] = Map(TestClasses.dataTypeName -> testFactory)
      val instantiator = system.actorOf(Props(new DataTypeInstantiator(testFactories)))
      val dataTypeInstanceId = DataTypeInstanceId()

      instantiator ! UpdateResponse(dataTypeInstanceId, TestClasses.dataTypeName, "")

      val msg = expectMsgClass(classOf[NewDataTypeCreated])
      msg.dataTypeInstanceId should equal(dataTypeInstanceId)
      msg.ref shouldNot be(null)
    }

    "throw an exception when receiving an UpdateResponse with unknown data type name" in {
      val instantiator: TestActorRef[DataTypeInstantiator] = TestActorRef(Props(new DataTypeInstantiator(Map.empty)))
      val dataTypeInstanceId = DataTypeInstanceId()

      EventFilter[IllegalArgumentException](occurrences = 1) intercept {
        instantiator ! UpdateResponse(dataTypeInstanceId, TestClasses.dataTypeName, "")
      }
    }
  }
}
