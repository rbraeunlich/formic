package de.tu_berlin.formic.server.datatype

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import de.tu_berlin.formic.StopSystemAfterAll
import de.tu_berlin.formic.common.{ClientId, DataTypeInstanceId}
import de.tu_berlin.formic.common.datatype.{AbstractDataType, DataTypeName, DataTypeOperation}
import de.tu_berlin.formic.common.message.CreateRequest
import org.scalatest.{Matchers, WordSpecLike}



class TestDataTypeFactory extends AbstractDataTypeFactory[TestDataType] {
  override def create(): TestDataType = {
    new TestDataType()
  }

  override val name: DataTypeName = DataTypeName("Test")
}

class TestDataType extends AbstractDataType {
  override val dataTypeName: DataTypeName = DataTypeName("Test")

  override def apply(op: DataTypeOperation): Unit = {}

  override def getDataAsJson: String = ""
}

/**
  * @author Ronny Bräunlich
  */

class AbstractDataTypeFactorySpec extends TestKit(ActorSystem("testsystem"))
  with WordSpecLike
  with ImplicitSender
  with StopSystemAfterAll
  with Matchers {

  "The factory" must {
    "create a new data type instance when receiving a CreateRequest" in {
      val factory = system.actorOf(Props[TestDataTypeFactory], "factory")
      val dataTypeInstanceId = DataTypeInstanceId()
      factory ! CreateRequest(ClientId(), dataTypeInstanceId, DataTypeName("Test"))

      val received = expectMsgClass(classOf[NewDataTypeCreated])
      received.dataTypeInstanceId should be(dataTypeInstanceId)
      received.ref shouldNot be(null)
    }
  }


}
