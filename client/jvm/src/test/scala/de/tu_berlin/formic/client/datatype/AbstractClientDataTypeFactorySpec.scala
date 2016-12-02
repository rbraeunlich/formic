package de.tu_berlin.formic.client.datatype

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import de.tu_berlin.formic.client.DataTypeInstantiator.WrappedCreateRequest
import de.tu_berlin.formic.client.datatype.AbstractClientDataTypeFactory.NewDataTypeCreated
import de.tu_berlin.formic.client.{StopSystemAfterAll, TestControlAlgorithm}
import de.tu_berlin.formic.common.{ClientId, DataTypeInstanceId}
import de.tu_berlin.formic.common.datatype._
import de.tu_berlin.formic.common.message.CreateRequest
import org.scalatest.{Matchers, WordSpecLike}

/**
  * @author Ronny Bräunlich
  */
class AbstractClientDataTypeFactorySpec extends TestKit(ActorSystem("AbstractClientDataTypeFactorySpec"))
  with WordSpecLike
  with ImplicitSender
  with StopSystemAfterAll
  with Matchers {

  "AbstractClientDataTypeFactory" must {

    "create a data type and data type wrapper when receiving a WrappedCreateRequest" in {
      val factory = system.actorOf(Props(new AbstractClientDataTypeFactorySpecFactory))
      val outgoing = TestProbe()
      val instanceId = DataTypeInstanceId()

      factory ! WrappedCreateRequest(outgoing.ref, CreateRequest(ClientId(), instanceId, DataTypeName("AbstractClientDataTypeFactorySpec") ))

      val msg = expectMsgClass(classOf[NewDataTypeCreated])
      msg.dataTypeInstanceId should equal(instanceId)
      msg.dataTypeActor should not equal null
      msg.wrapper shouldBe a[AbstractClientDataTypeFactorySpecFormicDataType]
    }

  }

}

class AbstractClientDataTypeFactorySpecServerDataType extends AbstractServerDataType(DataTypeInstanceId(), TestControlAlgorithm) {
  override val dataTypeName: DataTypeName = DataTypeName("AbstractClientDataTypeFactorySpec")

  override val transformer: OperationTransformer = null

  override def apply(op: DataTypeOperation): Unit = {}

  override def getDataAsJson: String = ""
}

class AbstractClientDataTypeFactorySpecFormicDataType extends FormicDataType {
  override val dataTypeName: DataTypeName = DataTypeName("AbstractClientDataTypeFactorySpec")
  override var callback: () => Unit = _
}

class AbstractClientDataTypeFactorySpecFactory extends AbstractClientDataTypeFactory[AbstractClientDataTypeFactorySpecServerDataType, AbstractClientDataTypeFactorySpecFormicDataType](null) {

  override val name: DataTypeName = DataTypeName("AbstractClientDataTypeFactorySpec")

  override def createDataType(dataTypeInstanceId: DataTypeInstanceId, outgoingConnection: ActorRef): AbstractClientDataTypeFactorySpecServerDataType = {
    new AbstractClientDataTypeFactorySpecServerDataType
  }

  override def createWrapperType(dataTypeInstanceId: DataTypeInstanceId, dataType: ActorRef): AbstractClientDataTypeFactorySpecFormicDataType = {
    new AbstractClientDataTypeFactorySpecFormicDataType
  }
}