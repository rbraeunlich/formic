package de.tu_berlin.formic.common.datatype

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestActorRef, TestKit, TestProbe}
import de.tu_berlin.formic.common.controlalgo.ControlAlgorithm
import de.tu_berlin.formic.common.message.{HistoricOperationRequest, OperationMessage, UpdateRequest, UpdateResponse}
import de.tu_berlin.formic.common.{ClientId, DataTypeInstanceId, OperationId}
import org.scalatest.Assertions._
import org.scalatest.{Matchers, WordSpecLike}

class AbstractServerDataTypeSpecTestServerDataType(override val historyBuffer: HistoryBuffer, id: DataTypeInstanceId, controlAlgorithm: ControlAlgorithm) extends AbstractServerDataType(id, controlAlgorithm) {

  val transformer = new OperationTransformer {
    override def transform(pair: (DataTypeOperation, DataTypeOperation)): DataTypeOperation = pair._1
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
class AbstractServerDataTypeSpec extends TestKit(ActorSystem("AbstractDataTypeSpec"))
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
    }

    "apply received operations from an operation message and publish the same message again" in {
      val probe = TestProbe()
      system.eventStream.subscribe(probe.ref, classOf[OperationMessage])
      val dataType = system.actorOf(Props(new AbstractServerDataTypeSpecTestServerDataType(new HistoryBuffer, DataTypeInstanceId(), new AbstractServerDataTypeSpecTestControlAlgorithm)))
      val message = OperationMessage(
        ClientId(),
        DataTypeInstanceId(),
        AbstractServerDataTypeSpec.dataTypeName,
        List(AbstractServerDataTypeSpecTestOperation(OperationId(), OperationContext(List.empty), ClientId()))
      )
      dataType ! message

      probe.expectMsg(message)
    }

    "add applied operations to the history buffer" in {
      val dataType: TestActorRef[AbstractServerDataType] = TestActorRef(Props(new AbstractServerDataTypeSpecTestServerDataType(new HistoryBuffer, DataTypeInstanceId(), new AbstractServerDataTypeSpecTestControlAlgorithm)))
      val operation = AbstractServerDataTypeSpecTestOperation(OperationId(), OperationContext(List.empty), ClientId())
      val message = OperationMessage(
        ClientId(),
        DataTypeInstanceId(),
        AbstractServerDataTypeSpec.dataTypeName,
        List(operation)
      )
      dataType ! message

      dataType.underlyingActor.historyBuffer.history should contain(operation)
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

      expectMsg(OperationMessage(clientId, dataTypeInstanceId, AbstractServerDataTypeSpec.dataTypeName, List(op2)))
    }

    "answer an UpdateRequest with its full data in an UpdateResponse" in {
      val probe = TestProbe()
      system.eventStream.subscribe(probe.ref, classOf[UpdateResponse])
      val clientId = ClientId()
      val dataType = system.actorOf(Props(new AbstractServerDataTypeSpecTestServerDataType(new HistoryBuffer, DataTypeInstanceId(), new AbstractServerDataTypeSpecTestControlAlgorithm)))
      val msg = probe.expectMsgClass(classOf[UpdateResponse])
      val dataTypeInstanceId = msg.dataTypeInstanceId

      dataType ! UpdateRequest(clientId, dataTypeInstanceId)

      expectMsg(UpdateResponse(dataTypeInstanceId, AbstractServerDataTypeSpec.dataTypeName, "{data}"))
    }

    "not apply operations when the ControlAlgorithm states they are not ready and store them" in {
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
      dataType.underlyingActor.causallyNotReadyOperations should contain(operation)
    }

    "apply previously stored operations when they become causally ready" in {
      val controlAlgo = new ControlAlgorithm {
        override def transform(op: DataTypeOperation, history: HistoryBuffer, transformer: OperationTransformer): DataTypeOperation = {
          op
        }

        override def canBeApplied(op: DataTypeOperation, history: HistoryBuffer): Boolean = {
          val directAncestorOperation = op.operationContext.operations.headOption
          directAncestorOperation match {
            case None => true
            case Some(ancestorId) =>
              val foundInHistory = history.findOperation(ancestorId)
              foundInHistory match {
                case None => false
                case Some(_) => true
              }
          }
        }
      }
      val dataType: TestActorRef[AbstractServerDataType] = TestActorRef(Props(new AbstractServerDataTypeSpecTestServerDataType(new HistoryBuffer, DataTypeInstanceId(), controlAlgo)))
      val triggerOperationId = OperationId()
      val operation = AbstractServerDataTypeSpecTestOperation(OperationId(), OperationContext(List(triggerOperationId)), ClientId())
      val operation2 = AbstractServerDataTypeSpecTestOperation(OperationId(), OperationContext(List(operation.id)), ClientId())
      val operation3 = AbstractServerDataTypeSpecTestOperation(triggerOperationId, OperationContext(List.empty), ClientId())
      val message = OperationMessage(
        ClientId(),
        DataTypeInstanceId(),
        AbstractServerDataTypeSpec.dataTypeName,
        List(operation)
      )

      dataType ! message

      val message2 = OperationMessage(
        ClientId(),
        DataTypeInstanceId(),
        AbstractServerDataTypeSpec.dataTypeName,
        List(operation2)
      )
      dataType ! message2
      dataType.underlyingActor.historyBuffer.history should not contain operation
      dataType.underlyingActor.historyBuffer.history should not contain operation2

      val message3 = OperationMessage(
        ClientId(),
        DataTypeInstanceId(),
        AbstractServerDataTypeSpec.dataTypeName,
        List(operation3)
      )

      dataType ! message3

      dataType.underlyingActor.historyBuffer.history should contain inOrderOnly(operation2, operation, operation3)
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

      val dataType: TestActorRef[AbstractServerDataType] = TestActorRef(Props(new AbstractServerDataTypeSpecTestServerDataType(new HistoryBuffer, DataTypeInstanceId(), controlAlgo)))
      val operation = AbstractServerDataTypeSpecTestOperation(OperationId(), OperationContext(List.empty), ClientId())
      val message = OperationMessage(
        ClientId(),
        DataTypeInstanceId(),
        AbstractServerDataTypeSpec.dataTypeName,
        List(operation)
      )

      dataType ! message

      hasBeenTransformed should be(true)
    }

    "ignore duplicated operation messages" in {
      val dataType: TestActorRef[AbstractServerDataType] = TestActorRef(Props(new AbstractServerDataTypeSpecTestServerDataType(new HistoryBuffer, DataTypeInstanceId(), new AbstractServerDataTypeSpecTestControlAlgorithm)))
      val operation = AbstractServerDataTypeSpecTestOperation(OperationId(), OperationContext(List.empty), ClientId())
      val message = OperationMessage(
        ClientId(),
        DataTypeInstanceId(),
        AbstractServerDataTypeSpec.dataTypeName,
        List(operation)
      )

      dataType ! message
      dataType ! message

      dataType.underlyingActor.historyBuffer.history should be(List(operation))
    }
  }
}

object AbstractServerDataTypeSpec {
  val dataTypeName = DataTypeName("Test")
}
