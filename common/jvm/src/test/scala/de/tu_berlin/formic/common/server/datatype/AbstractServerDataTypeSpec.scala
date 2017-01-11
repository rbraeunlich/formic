package de.tu_berlin.formic.common.server.datatype

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestActorRef, TestKit, TestProbe}
import de.tu_berlin.formic.common.controlalgo.ControlAlgorithm
import de.tu_berlin.formic.common.datatype._
import de.tu_berlin.formic.common.message.{HistoricOperationRequest, OperationMessage, UpdateRequest, UpdateResponse}
import de.tu_berlin.formic.common.server.datatype.AbstractServerDataType.{GetHistory, HistoricOperationsAnswer}
import de.tu_berlin.formic.common.{ClientId, DataTypeInstanceId, OperationId}
import org.scalatest.Assertions._
import org.scalatest.{Matchers, WordSpecLike}

class AbstractServerDataTypeSpecTestServerDataType(override val historyBuffer: HistoryBuffer, id: DataTypeInstanceId, controlAlgorithm: ControlAlgorithm) extends AbstractServerDataType(id, controlAlgorithm) {

  val transformer = new OperationTransformer {
    override def transform(pair: (DataTypeOperation, DataTypeOperation)): DataTypeOperation = pair._1
    override def bulkTransform(operation: DataTypeOperation, bridge: List[DataTypeOperation]): List[DataTypeOperation] = bridge

    override protected def transformInternal(pair: (DataTypeOperation, DataTypeOperation), withNewContext: Boolean): DataTypeOperation = pair._1
  }

  var data = "{data}"

  override def apply(op: DataTypeOperation): Unit = {
    op match {
      case test: AbstractServerDataTypeSpecTestOperation => data = "{received}"
      case _ => fail
    }
  }

  override val dataTypeName: DataTypeName = AbstractServerDataTypeSpec.dataTypeName

  override def getDataAsJson: String = data
}

case class AbstractServerDataTypeSpecTestOperation(id: OperationId, operationContext: OperationContext, var clientId: ClientId) extends DataTypeOperation

class AbstractServerDataTypeSpecTestControlAlgorithm(applied: Boolean = true) extends ControlAlgorithm {

  override def canBeApplied(op: DataTypeOperation, history: HistoryBuffer): Boolean = applied

  override def transform(op: DataTypeOperation, history: HistoryBuffer, transformer: OperationTransformer): DataTypeOperation = op
}

/**
  * @author Ronny Bräunlich
  */
class AbstractServerDataTypeSpec extends TestKit(ActorSystem("AbstractServerDataTypeSpec"))
  with WordSpecLike
  with ImplicitSender
  with StopSystemAfterAll
  with Matchers {

  "An AbstractDataType" must {

    "publish an UpdateResponse after creation" in {
      val probe = TestProbe()
      system.eventStream.subscribe(probe.ref, classOf[UpdateResponse])

      system.actorOf(Props(new AbstractServerDataTypeSpecTestServerDataType(new HistoryBuffer, DataTypeInstanceId(), new AbstractServerDataTypeSpecTestControlAlgorithm)))

      val msg = probe.expectMsgClass(classOf[UpdateResponse])
      msg.dataType should be(AbstractServerDataTypeSpec.dataTypeName)
      msg.data should be("{data}")
      msg.lastOperationId shouldBe empty
    }

    "apply received operations from an operation message and publish the same message again if no transformations took place" in {
      val probe = TestProbe()
      system.eventStream.subscribe(probe.ref, classOf[OperationMessage])
      val dataTypeInstanceId: DataTypeInstanceId = DataTypeInstanceId()
      val clientId: ClientId = ClientId()
      val dataType = system.actorOf(Props(new AbstractServerDataTypeSpecTestServerDataType(new HistoryBuffer, dataTypeInstanceId, new AbstractServerDataTypeSpecTestControlAlgorithm)))
      val message = OperationMessage(
        clientId,
        dataTypeInstanceId,
        AbstractServerDataTypeSpec.dataTypeName,
        List(AbstractServerDataTypeSpecTestOperation(OperationId(), OperationContext(List.empty), clientId))
      )
      dataType ! message

      probe.expectMsg(message)
    }

    "add applied operations to the history buffer" in {
      val dataTypeInstanceId: DataTypeInstanceId = DataTypeInstanceId()
      val clientId: ClientId = ClientId()
      val dataType = system.actorOf(Props(new AbstractServerDataTypeSpecTestServerDataType(new HistoryBuffer, dataTypeInstanceId, new AbstractServerDataTypeSpecTestControlAlgorithm)))
      val operation = AbstractServerDataTypeSpecTestOperation(OperationId(), OperationContext(List.empty), clientId)
      val message = OperationMessage(
        clientId,
        dataTypeInstanceId,
        AbstractServerDataTypeSpec.dataTypeName,
        List(operation)
      )
      dataType ! message

      dataType ! GetHistory

      expectMsgClass(classOf[HistoryBuffer]).history should contain(operation)
    }

    "return the requested historic operations when receiving HistoricOperationsRequest" in {
      val probe = TestProbe()
      system.eventStream.subscribe(probe.ref, classOf[UpdateResponse])

      val op1 = AbstractServerDataTypeSpecTestOperation(OperationId(), OperationContext(List.empty), ClientId())
      val op2 = AbstractServerDataTypeSpecTestOperation(OperationId(), OperationContext(List(op1.id)), ClientId())
      val historyBuffer = new HistoryBuffer
      historyBuffer.addOperation(op1)
      historyBuffer.addOperation(op2)
      val clientId = ClientId()
      val dataType = system.actorOf(Props(new AbstractServerDataTypeSpecTestServerDataType(historyBuffer, DataTypeInstanceId(), new AbstractServerDataTypeSpecTestControlAlgorithm)))
      val msg = probe.expectMsgClass(classOf[UpdateResponse])
      val dataTypeInstanceId = msg.dataTypeInstanceId

      dataType ! HistoricOperationRequest(clientId, dataTypeInstanceId, op1.id)

      expectMsg(HistoricOperationsAnswer(OperationMessage(clientId, dataTypeInstanceId, AbstractServerDataTypeSpec.dataTypeName, List(op2))))
    }

    "answer an UpdateRequest with its full data in an UpdateResponse" in {
      val probe = TestProbe()
      system.eventStream.subscribe(probe.ref, classOf[UpdateResponse])
      val clientId = ClientId()
      val dataType = system.actorOf(Props(new AbstractServerDataTypeSpecTestServerDataType(new HistoryBuffer, DataTypeInstanceId(), new AbstractServerDataTypeSpecTestControlAlgorithm)))
      val msg = probe.expectMsgClass(classOf[UpdateResponse])
      val dataTypeInstanceId = msg.dataTypeInstanceId
      //add something to the history
      val operationId = OperationId()
      val operation = AbstractServerDataTypeSpecTestOperation(operationId, OperationContext(List.empty), ClientId())
      val operationMessage = OperationMessage(ClientId(), DataTypeInstanceId(), AbstractServerDataTypeSpec.dataTypeName, List(operation))
      dataType ! operationMessage

      dataType ! UpdateRequest(clientId, dataTypeInstanceId)

      expectMsg(UpdateResponse(dataTypeInstanceId, AbstractServerDataTypeSpec.dataTypeName, "{received}", Option(operationId)))
    }

    "not apply operations when the ControlAlgorithm states they are not ready" in {
      val dataType: TestActorRef[AbstractServerDataType] = TestActorRef(Props(new AbstractServerDataTypeSpecTestServerDataType(new HistoryBuffer, DataTypeInstanceId(), new AbstractServerDataTypeSpecTestControlAlgorithm(false))))
      val operation = AbstractServerDataTypeSpecTestOperation(OperationId(), OperationContext(List(OperationId())), ClientId())
      val message = OperationMessage(
        ClientId(),
        DataTypeInstanceId(),
        AbstractServerDataTypeSpec.dataTypeName,
        List(operation)
      )
      dataType ! message

      dataType.underlyingActor.historyBuffer.history should not contain operation
    }

    "pass operations to the control algorithm for transformation" in {
      var hasBeenTransformed = false
      val controlAlgo: ControlAlgorithm = new ControlAlgorithm {
        override def transform(op: DataTypeOperation, history: HistoryBuffer, transformer: OperationTransformer): DataTypeOperation = {
          hasBeenTransformed = true
          op
        }

        override def canBeApplied(op: DataTypeOperation, history: HistoryBuffer): Boolean = {
          true
        }
      }

      val dataType = system.actorOf(Props(new AbstractServerDataTypeSpecTestServerDataType(new HistoryBuffer, DataTypeInstanceId(), controlAlgo)))
      val operation = AbstractServerDataTypeSpecTestOperation(OperationId(), OperationContext(List.empty), ClientId())
      val message = OperationMessage(
        ClientId(),
        DataTypeInstanceId(),
        AbstractServerDataTypeSpec.dataTypeName,
        List(operation)
      )

      dataType ! message

      awaitAssert(hasBeenTransformed should be(true))
    }

    "ignore duplicated operation messages" in {
      val dataType = system.actorOf(Props(new AbstractServerDataTypeSpecTestServerDataType(new HistoryBuffer, DataTypeInstanceId(), new AbstractServerDataTypeSpecTestControlAlgorithm)))
      val operation = AbstractServerDataTypeSpecTestOperation(OperationId(), OperationContext(List.empty), ClientId())
      val message = OperationMessage(
        ClientId(),
        DataTypeInstanceId(),
        AbstractServerDataTypeSpec.dataTypeName,
        List(operation)
      )

      dataType ! message
      dataType ! message

      dataType ! GetHistory
      expectMsgClass(classOf[HistoryBuffer]).history should be(List(operation))
    }

    "publish the transformed operation if a transformation took place" in {
      val dataTypeInstanceId: DataTypeInstanceId = DataTypeInstanceId()
      val clientId: ClientId = ClientId()
      val probe = TestProbe()
      system.eventStream.subscribe(probe.ref, classOf[OperationMessage])
      val originalOperation = AbstractServerDataTypeSpecTestOperation(OperationId(), OperationContext(List.empty), clientId)
      val transformedOperation = AbstractServerDataTypeSpecTestOperation(OperationId(), OperationContext(List(OperationId())), clientId)
      val controlAlgorithm = new ControlAlgorithm {

        override def transform(op: DataTypeOperation, history: HistoryBuffer, transformer: OperationTransformer): DataTypeOperation = transformedOperation

        override def canBeApplied(op: DataTypeOperation, history: HistoryBuffer): Boolean = true

      }
      val dataType = system.actorOf(Props(new AbstractServerDataTypeSpecTestServerDataType(new HistoryBuffer, dataTypeInstanceId, controlAlgorithm)))
      val message = OperationMessage(clientId, dataTypeInstanceId, AbstractServerDataTypeSpec.dataTypeName, List(originalOperation))
      dataType ! message

      probe.expectMsg(OperationMessage(message.clientId, message.dataTypeInstanceId, message.dataType, List(transformedOperation)))
    }
  }
}

object AbstractServerDataTypeSpec {
  val dataTypeName = DataTypeName("Test")
}
