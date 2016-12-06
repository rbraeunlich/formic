package de.tu_berlin.formic.common.datatype

import akka.actor.{ActorRef, ActorSystem, Props, Terminated}
import akka.testkit.{ImplicitSender, TestActorRef, TestKit, TestProbe}
import de.tu_berlin.formic.common.controlalgo.ControlAlgorithmClient
import de.tu_berlin.formic.common.datatype.FormicDataType.LocalOperationMessage
import de.tu_berlin.formic.common.datatype.client.AbstractClientDataType
import de.tu_berlin.formic.common.datatype.client.AbstractClientDataType.ReceiveCallback
import de.tu_berlin.formic.common.message.{OperationMessage, UpdateRequest}
import de.tu_berlin.formic.common.{ClientId, DataTypeInstanceId, OperationId}
import org.scalatest.Assertions._
import org.scalatest.{Matchers, WordSpecLike}
import scala.concurrent.duration._
/**
  * @author Ronny Bräunlich
  */
class AbstractClientDataTypeSpec extends TestKit(ActorSystem("AbstractDataTypeSpec"))
  with WordSpecLike
  with ImplicitSender
  with StopSystemAfterAll
  with Matchers {

  "AbstractClientDataType" must {

    "ignore messages until callback is set" in {
      val dataType: TestActorRef[AbstractClientDataTypeTestClientDataType] = TestActorRef(Props(new AbstractClientDataTypeTestClientDataType(DataTypeInstanceId(), new AbstractClientDataTypeSpecControlAlgorithmClient)))
      val operationMessage = OperationMessage(
        ClientId(),
        DataTypeInstanceId(),
        AbstractClientDataTypeSpec.dataTypeName,
        List(
          AbstractClientDataTypeSpecTestOperation(OperationId(), OperationContext(List.empty), ClientId(), "")
        )
      )

      dataType ! LocalOperationMessage(operationMessage)
      expectNoMsg()
      dataType ! operationMessage
      expectNoMsg()
      dataType ! UpdateRequest(ClientId(), DataTypeInstanceId())
      expectNoMsg()

      dataType.underlyingActor.historyBuffer.history shouldBe empty
    }

    "apply received local operations immediately from an local operation message" in {
      val dataTypeInstanceId = DataTypeInstanceId()
      val data = "{foo}"
      val dataType: TestActorRef[AbstractClientDataTypeTestClientDataType] = TestActorRef(Props(new AbstractClientDataTypeTestClientDataType(dataTypeInstanceId, new AbstractClientDataTypeSpecControlAlgorithmClient)))
      dataType ! ReceiveCallback(() => {})
      val operationMessage = OperationMessage(
        ClientId(),
        dataTypeInstanceId,
        AbstractClientDataTypeSpec.dataTypeName,
        List(
          AbstractClientDataTypeSpecTestOperation(OperationId(), OperationContext(List.empty), ClientId(), data)
        )
      )

      dataType ! LocalOperationMessage(operationMessage)

      dataType.underlyingActor.data should equal(data)
    }

    "not transform local operations" in {
      val dataTypeInstanceId = DataTypeInstanceId()
      val data = "{foo}"
      val controlAlgo = new AbstractClientDataTypeSpecControlAlgorithmClient
      val dataType: TestActorRef[AbstractClientDataTypeTestClientDataType] = TestActorRef(Props(new AbstractClientDataTypeTestClientDataType(dataTypeInstanceId, controlAlgo)))
      dataType ! ReceiveCallback(() => {})
      val operationMessage = OperationMessage(
        ClientId(),
        dataTypeInstanceId,
        AbstractClientDataTypeSpec.dataTypeName,
        List(
          AbstractClientDataTypeSpecTestOperation(OperationId(), OperationContext(List.empty), ClientId(), data)
        )
      )

      dataType ! LocalOperationMessage(operationMessage)

      controlAlgo.didTransform should equal(false)
    }

    "add local operations to the history buffer" in {
      val dataTypeInstanceId = DataTypeInstanceId()
      val data = "{foo}"
      val dataType: TestActorRef[AbstractClientDataTypeTestClientDataType] = TestActorRef(Props(new AbstractClientDataTypeTestClientDataType(dataTypeInstanceId, new AbstractClientDataTypeSpecControlAlgorithmClient)))
      dataType ! ReceiveCallback(() => {})
      val operation = AbstractClientDataTypeSpecTestOperation(OperationId(), OperationContext(List.empty), ClientId(), data)
      val operationMessage = OperationMessage(
        ClientId(),
        dataTypeInstanceId,
        AbstractClientDataTypeSpec.dataTypeName,
        List(operation)
      )

      dataType ! LocalOperationMessage(operationMessage)

      dataType.underlyingActor.historyBuffer.history should contain(operation)
    }

    "update the operation context of local operations" in {
      val dataTypeInstanceId = DataTypeInstanceId()
      val data = "{foo}"
      val dataType: TestActorRef[AbstractClientDataTypeTestClientDataType] = TestActorRef(Props(new AbstractClientDataTypeTestClientDataType(dataTypeInstanceId, new AbstractClientDataTypeSpecControlAlgorithmClient)))
      dataType ! ReceiveCallback(() => {})
      val operation = AbstractClientDataTypeSpecTestOperation(OperationId(), OperationContext(List.empty), ClientId(), data)
      val operation2 = AbstractClientDataTypeSpecTestOperation(OperationId(), OperationContext(List.empty), ClientId(), data)
      val operationMessage = OperationMessage(
        ClientId(),
        dataTypeInstanceId,
        AbstractClientDataTypeSpec.dataTypeName,
        List(operation)
      )
      val operationMessage2 = OperationMessage(
        ClientId(),
        dataTypeInstanceId,
        AbstractClientDataTypeSpec.dataTypeName,
        List(operation2)
      )

      dataType ! LocalOperationMessage(operationMessage)
      dataType ! LocalOperationMessage(operationMessage2)

      dataType.underlyingActor.historyBuffer.history.head should equal(AbstractClientDataTypeSpecTestOperation(operation2.id, OperationContext(List(operation.id)), operation2.clientId, operation2.data))
    }

    "add remote operations to the history buffer" in {
      val dataTypeInstanceId = DataTypeInstanceId()
      val data = "{foo}"
      val dataType: TestActorRef[AbstractClientDataTypeTestClientDataType] = TestActorRef(Props(new AbstractClientDataTypeTestClientDataType(dataTypeInstanceId, new AbstractClientDataTypeSpecControlAlgorithmClient)))
      dataType ! ReceiveCallback(() => {})
      val operation = AbstractClientDataTypeSpecTestOperation(OperationId(), OperationContext(List.empty), ClientId(), data)
      val operationMessage = OperationMessage(
        ClientId(),
        dataTypeInstanceId,
        AbstractClientDataTypeSpec.dataTypeName,
        List(operation)
      )

      dataType ! operationMessage

      dataType.underlyingActor.historyBuffer.history should contain(operation)
    }

    "apply received operations from an operation message" in {
      val dataTypeInstanceId = DataTypeInstanceId()
      val dataType: TestActorRef[AbstractClientDataTypeTestClientDataType] = TestActorRef(Props(new AbstractClientDataTypeTestClientDataType(dataTypeInstanceId, new AbstractClientDataTypeSpecControlAlgorithmClient)))
      dataType ! ReceiveCallback(() => {})
      val data = "{foo}"
      val operation = AbstractClientDataTypeSpecTestOperation(OperationId(), OperationContext(List.empty), ClientId(), data)
      val operationMessage = OperationMessage(
        ClientId(),
        dataTypeInstanceId,
        AbstractClientDataTypeSpec.dataTypeName,
        List(operation)
      )

      dataType ! LocalOperationMessage(operationMessage)

      dataType.underlyingActor.data should equal(data)
    }

    "not apply operations when the ControlAlgorithm states they are not ready and store them" in {
      val dataTypeInstanceId = DataTypeInstanceId()
      val dataType: TestActorRef[AbstractClientDataTypeTestClientDataType] = TestActorRef(Props(new AbstractClientDataTypeTestClientDataType(dataTypeInstanceId, new AbstractClientDataTypeSpecControlAlgorithmClient(false))))
      dataType ! ReceiveCallback(() => {})
      val operation = AbstractClientDataTypeSpecTestOperation(OperationId(), OperationContext(List.empty), ClientId(), "{}")
      val message = OperationMessage(
        ClientId(),
        DataTypeInstanceId(),
        AbstractClientDataTypeSpec.dataTypeName,
        List(operation)
      )
      dataType ! message

      dataType.underlyingActor.historyBuffer.history should not contain operation
    }

    "pass operations to the control algorithm for transformation" in {
      var hasBeenTransformed = false
      val controlAlgo: ControlAlgorithmClient = new ControlAlgorithmClient {
        override def transform(op: DataTypeOperation, history: HistoryBuffer, transformer: OperationTransformer): DataTypeOperation = {
          hasBeenTransformed = true
          op
        }

        override def canBeApplied(op: DataTypeOperation, history: HistoryBuffer): Boolean = {
          true
        }

        override def canLocalOperationBeApplied(op: DataTypeOperation): Boolean = true
      }

      val dataType: TestActorRef[AbstractClientDataTypeTestClientDataType] = TestActorRef(Props(new AbstractClientDataTypeTestClientDataType(DataTypeInstanceId(), controlAlgo)))
      dataType ! ReceiveCallback(() => {})
      val operation = AbstractClientDataTypeSpecTestOperation(OperationId(), OperationContext(List.empty), ClientId(), "{1}")
      val message = OperationMessage(
        ClientId(),
        DataTypeInstanceId(),
        AbstractClientDataTypeSpec.dataTypeName,
        List(operation)
      )

      dataType ! message

      hasBeenTransformed should be(true)
    }

    "replace a callback when receiving a new one and kill the old one" in {
      val dataType: TestActorRef[AbstractClientDataTypeTestClientDataType] = TestActorRef(Props(new AbstractClientDataTypeTestClientDataType(DataTypeInstanceId(), new AbstractClientDataTypeSpecControlAlgorithmClient(false))))
      dataType ! ReceiveCallback(() => {})
      val oldCallback = dataType.children.head
      val watcher = TestProbe()
      watcher.watch(oldCallback)

      dataType ! ReceiveCallback(() => {})

      watcher.expectMsgPF(2.seconds){case Terminated(ref) => ref should equal(oldCallback)}
    }
  }
}

class AbstractClientDataTypeTestClientDataType(dataTypeInstanceId: DataTypeInstanceId, clientControlAlgorithm: ControlAlgorithmClient) extends AbstractClientDataType(dataTypeInstanceId, clientControlAlgorithm) {

  var data = "{test}"

  override val dataTypeName: DataTypeName = AbstractClientDataTypeSpec.dataTypeName

  override val transformer: OperationTransformer = new OperationTransformer {
    override def transform(pair: (DataTypeOperation, DataTypeOperation)): DataTypeOperation = pair._1
  }

  override def apply(op: DataTypeOperation): Unit = {
    op match {
      case test: AbstractClientDataTypeSpecTestOperation => data = test.data
      case _ => fail
    }
  }

  override def getDataAsJson: String = data

  override def cloneOperationWithNewContext(op: DataTypeOperation, context: OperationContext): DataTypeOperation = {
    op match {
      case abstr: AbstractClientDataTypeSpecTestOperation => AbstractClientDataTypeSpecTestOperation(abstr.id, context, abstr.clientId, abstr.data)
    }
  }
}

case class AbstractClientDataTypeSpecTestOperation(id: OperationId, operationContext: OperationContext, var clientId: ClientId, data: String) extends DataTypeOperation

object AbstractClientDataTypeSpec {
  val dataTypeName = DataTypeName("AbstractClientDataType")
}

class AbstractClientDataTypeSpecControlAlgorithmClient(canRemoteBeApplied: Boolean = true) extends ControlAlgorithmClient {

  var didTransform = false

  override def canLocalOperationBeApplied(op: DataTypeOperation): Boolean = true

  override def canBeApplied(op: DataTypeOperation, history: HistoryBuffer): Boolean = canRemoteBeApplied

  override def transform(op: DataTypeOperation, history: HistoryBuffer, transformer: OperationTransformer): DataTypeOperation = {
    didTransform = true
    op
  }
}