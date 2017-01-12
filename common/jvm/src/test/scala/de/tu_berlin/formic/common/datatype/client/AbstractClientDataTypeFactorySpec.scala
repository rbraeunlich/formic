package de.tu_berlin.formic.common.datatype.client

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import de.tu_berlin.formic.common.controlalgo.ControlAlgorithmClient
import de.tu_berlin.formic.common.datatype._
import de.tu_berlin.formic.common.datatype.client.AbstractClientDataTypeFactory.{LocalCreateRequest, NewDataTypeCreated, WrappedCreateRequest}
import de.tu_berlin.formic.common.message.CreateRequest
import de.tu_berlin.formic.common.{ClientId, DataTypeInstanceId, OperationId}
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
      val clientId = ClientId()

      factory ! WrappedCreateRequest(outgoing.ref, null, Option.empty,CreateRequest(ClientId(), instanceId, DataTypeName("AbstractClientDataTypeFactorySpec") ), clientId)

      val msg = expectMsgClass(classOf[NewDataTypeCreated])
      msg.dataTypeInstanceId should equal(instanceId)
      msg.dataTypeActor should not equal null
      msg.wrapper shouldBe a[AbstractClientDataTypeFactorySpecFormicDataType]
      msg.wrapper.clientId should equal(clientId)
    }

    "create only a data type when receiving a LocalCreateRequest" in {
      val factory = system.actorOf(Props(new AbstractClientDataTypeFactorySpecFactory))
      val outgoing = TestProbe()
      val instanceId = DataTypeInstanceId()

      factory ! LocalCreateRequest(outgoing.ref, instanceId)

      val msg = expectMsgClass(classOf[NewDataTypeCreated])
      msg.dataTypeInstanceId should equal(instanceId)
      msg.dataTypeActor should not equal null
      msg.wrapper should be(null)
    }

  }

}

object AbstractClientDataTypeSpecControlAlgorithm extends ControlAlgorithmClient {

  override def canLocalOperationBeApplied(op: DataTypeOperation): Boolean = true

  override def canBeApplied(op: DataTypeOperation, history: HistoryBuffer): Boolean = true

  override def transform(op: DataTypeOperation, history: HistoryBuffer, transformer: OperationTransformer): DataTypeOperation = op

  override def currentOperationContext: OperationContext = OperationContext(List.empty)
}

class AbstractClientDataTypeFactorySpecServerDataType(outgoingConnection: ActorRef) extends AbstractClientDataType(DataTypeInstanceId(), AbstractClientDataTypeSpecControlAlgorithm, Option.empty, outgoingConnection) {
  override val dataTypeName: DataTypeName = DataTypeName("AbstractClientDataTypeFactorySpec")

  override val transformer: OperationTransformer = null

  override def apply(op: DataTypeOperation): Unit = {}

  override def getDataAsJson: String = ""

  override def cloneOperationWithNewContext(op: DataTypeOperation, context: OperationContext): DataTypeOperation = op
}

class AbstractClientDataTypeFactorySpecFormicDataType(clientId: ClientId) extends FormicDataType(null, DataTypeName("AbstractClientDataTypeFactorySpec"),null,clientId, DataTypeInstanceId(), new DataTypeInitiator {
  override def initDataType(dataType: FormicDataType): Unit = {}
}) {
}

class AbstractClientDataTypeFactorySpecFactory extends AbstractClientDataTypeFactory[AbstractClientDataTypeFactorySpecServerDataType, AbstractClientDataTypeFactorySpecFormicDataType] {

  override val name: DataTypeName = DataTypeName("AbstractClientDataTypeFactorySpec")

  override def createDataType(dataTypeInstanceId: DataTypeInstanceId, outgoingConnection: ActorRef, data: Option[String], lastOperationId: Option[OperationId]): AbstractClientDataTypeFactorySpecServerDataType = {
    new AbstractClientDataTypeFactorySpecServerDataType(outgoingConnection)
  }

  override def createWrapperType(dataTypeInstanceId: DataTypeInstanceId, dataType: ActorRef, localClientId: ClientId): AbstractClientDataTypeFactorySpecFormicDataType = {
    new AbstractClientDataTypeFactorySpecFormicDataType(localClientId)
  }
}