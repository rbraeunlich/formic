package de.tu_berlin.formic.common.datatype

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestActorRef, TestKit}
import de.tu_berlin.formic.common.controlalgo.ControlAlgorithmClient
import de.tu_berlin.formic.common.datatype.FormicDataType.LocalOperationMessage
import de.tu_berlin.formic.common.message.OperationMessage
import de.tu_berlin.formic.common.{ClientId, DataTypeInstanceId, OperationId}
import org.scalatest.Assertions._
import org.scalatest.{Matchers, WordSpecLike}

/**
  * @author Ronny Bräunlich
  */
class AbstractClientDataTypeSpec extends TestKit(ActorSystem("AbstractDataTypeSpec"))
  with WordSpecLike
  with ImplicitSender
  with StopSystemAfterAll
  with Matchers {

  "AbstractClientDataType" must {
    "apply received local operations immediately from an local operation message" in {
      val dataTypeInstanceId = DataTypeInstanceId()
      val data = "{foo}"
      val dataType: TestActorRef[AbstractClientDataTypeTestClientDataType] = TestActorRef(Props(new AbstractClientDataTypeTestClientDataType(dataTypeInstanceId, new AbstractClientDataTypeSpecControlAlgorithmClient)))
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

    "add remote operations to the history buffer" in {
      val dataTypeInstanceId = DataTypeInstanceId()
      val data = "{foo}"
      val dataType: TestActorRef[AbstractClientDataTypeTestClientDataType] = TestActorRef(Props(new AbstractClientDataTypeTestClientDataType(dataTypeInstanceId, new AbstractClientDataTypeSpecControlAlgorithmClient)))
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