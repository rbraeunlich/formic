package de.tu_berlin.formic.common.server.datatype

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestActorRef, TestKit, TestProbe}
import de.tu_berlin.formic.common.controlalgo.ControlAlgorithm
import de.tu_berlin.formic.common.datatype._
import de.tu_berlin.formic.common.message.{HistoricOperationRequest, OperationMessage, UpdateRequest, UpdateResponse}
import de.tu_berlin.formic.common.server.datatype.AbstractServerDataStructure.{GetHistory, HistoricOperationsAnswer}
import de.tu_berlin.formic.common.{ClientId, DataStructureInstanceId, OperationId}
import org.scalatest.Assertions._
import org.scalatest.{Matchers, WordSpecLike}

class AbstractServerDataTypeSpecTestServerDataStructure(override val historyBuffer: HistoryBuffer, id: DataStructureInstanceId, controlAlgorithm: ControlAlgorithm) extends AbstractServerDataStructure(id, controlAlgorithm) {

  val transformer = new OperationTransformer {
    override def transform(pair: (DataStructureOperation, DataStructureOperation)): DataStructureOperation = pair._1
    override def bulkTransform(operation: DataStructureOperation, bridge: List[DataStructureOperation]): List[DataStructureOperation] = bridge

    override protected def transformInternal(pair: (DataStructureOperation, DataStructureOperation), withNewContext: Boolean): DataStructureOperation = pair._1
  }

  var data = "{data}"

  override def apply(op: DataStructureOperation): Unit = {
    op match {
      case test: AbstractServerDataStructureSpecTestOperation => data = "{received}"
      case _ => fail
    }
  }

  override val dataTypeName: DataStructureName = AbstractServerDataTypeSpec.dataTypeName

  override def getDataAsJson: String = data
}

case class AbstractServerDataStructureSpecTestOperation(id: OperationId, operationContext: OperationContext, var clientId: ClientId) extends DataStructureOperation

class AbstractServerDataTypeSpecTestControlAlgorithm(applied: Boolean = true) extends ControlAlgorithm {

  override def canBeApplied(op: DataStructureOperation, history: HistoryBuffer): Boolean = applied

  override def transform(op: DataStructureOperation, history: HistoryBuffer, transformer: OperationTransformer): DataStructureOperation = op
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

      system.actorOf(Props(new AbstractServerDataTypeSpecTestServerDataStructure(new HistoryBuffer, DataStructureInstanceId(), new AbstractServerDataTypeSpecTestControlAlgorithm)))

      val msg = probe.expectMsgClass(classOf[UpdateResponse])
      msg.dataStructure should be(AbstractServerDataTypeSpec.dataTypeName)
      msg.data should be("{data}")
      msg.lastOperationId shouldBe empty
    }

    "apply received operations from an operation message and publish the same message again if no transformations took place" in {
      val probe = TestProbe()
      system.eventStream.subscribe(probe.ref, classOf[OperationMessage])
      val dataTypeInstanceId: DataStructureInstanceId = DataStructureInstanceId()
      val clientId: ClientId = ClientId()
      val dataStructure = system.actorOf(Props(new AbstractServerDataTypeSpecTestServerDataStructure(new HistoryBuffer, dataTypeInstanceId, new AbstractServerDataTypeSpecTestControlAlgorithm)))
      val message = OperationMessage(
        clientId,
        dataTypeInstanceId,
        AbstractServerDataTypeSpec.dataTypeName,
        List(AbstractServerDataStructureSpecTestOperation(OperationId(), OperationContext(List.empty), clientId))
      )
      dataStructure ! message

      probe.expectMsg(message)
    }

    "add applied operations to the history buffer" in {
      val dataTypeInstanceId: DataStructureInstanceId = DataStructureInstanceId()
      val clientId: ClientId = ClientId()
      val dataType = system.actorOf(Props(new AbstractServerDataTypeSpecTestServerDataStructure(new HistoryBuffer, dataTypeInstanceId, new AbstractServerDataTypeSpecTestControlAlgorithm)))
      val operation = AbstractServerDataStructureSpecTestOperation(OperationId(), OperationContext(List.empty), clientId)
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

      val op1 = AbstractServerDataStructureSpecTestOperation(OperationId(), OperationContext(List.empty), ClientId())
      val op2 = AbstractServerDataStructureSpecTestOperation(OperationId(), OperationContext(List(op1.id)), ClientId())
      val historyBuffer = new HistoryBuffer
      historyBuffer.addOperation(op1)
      historyBuffer.addOperation(op2)
      val clientId = ClientId()
      val dataType = system.actorOf(Props(new AbstractServerDataTypeSpecTestServerDataStructure(historyBuffer, DataStructureInstanceId(), new AbstractServerDataTypeSpecTestControlAlgorithm)))
      val msg = probe.expectMsgClass(classOf[UpdateResponse])
      val dataStructureInstanceId = msg.dataStructureInstanceId

      dataType ! HistoricOperationRequest(clientId, dataStructureInstanceId, op1.id)

      expectMsg(HistoricOperationsAnswer(OperationMessage(clientId, dataStructureInstanceId, AbstractServerDataTypeSpec.dataTypeName, List(op2))))
    }

    "answer an UpdateRequest with its full data in an UpdateResponse" in {
      val probe = TestProbe()
      system.eventStream.subscribe(probe.ref, classOf[UpdateResponse])
      val clientId = ClientId()
      val dataType = system.actorOf(Props(new AbstractServerDataTypeSpecTestServerDataStructure(new HistoryBuffer, DataStructureInstanceId(), new AbstractServerDataTypeSpecTestControlAlgorithm)))
      val msg = probe.expectMsgClass(classOf[UpdateResponse])
      val dataStructureInstanceId = msg.dataStructureInstanceId
      //add something to the history
      val operationId = OperationId()
      val operation = AbstractServerDataStructureSpecTestOperation(operationId, OperationContext(List.empty), ClientId())
      val operationMessage = OperationMessage(ClientId(), DataStructureInstanceId(), AbstractServerDataTypeSpec.dataTypeName, List(operation))
      dataType ! operationMessage

      dataType ! UpdateRequest(clientId, dataStructureInstanceId)

      expectMsg(UpdateResponse(dataStructureInstanceId, AbstractServerDataTypeSpec.dataTypeName, "{received}", Option(operationId)))
    }

    "not apply operations when the ControlAlgorithm states they are not ready" in {
      val dataType: TestActorRef[AbstractServerDataStructure] = TestActorRef(Props(new AbstractServerDataTypeSpecTestServerDataStructure(new HistoryBuffer, DataStructureInstanceId(), new AbstractServerDataTypeSpecTestControlAlgorithm(false))))
      val operation = AbstractServerDataStructureSpecTestOperation(OperationId(), OperationContext(List(OperationId())), ClientId())
      val message = OperationMessage(
        ClientId(),
        DataStructureInstanceId(),
        AbstractServerDataTypeSpec.dataTypeName,
        List(operation)
      )
      dataType ! message

      dataType.underlyingActor.historyBuffer.history should not contain operation
    }

    "pass operations to the control algorithm for transformation" in {
      var hasBeenTransformed = false
      val controlAlgo: ControlAlgorithm = new ControlAlgorithm {
        override def transform(op: DataStructureOperation, history: HistoryBuffer, transformer: OperationTransformer): DataStructureOperation = {
          hasBeenTransformed = true
          op
        }

        override def canBeApplied(op: DataStructureOperation, history: HistoryBuffer): Boolean = {
          true
        }
      }

      val dataType = system.actorOf(Props(new AbstractServerDataTypeSpecTestServerDataStructure(new HistoryBuffer, DataStructureInstanceId(), controlAlgo)))
      val operation = AbstractServerDataStructureSpecTestOperation(OperationId(), OperationContext(List.empty), ClientId())
      val message = OperationMessage(
        ClientId(),
        DataStructureInstanceId(),
        AbstractServerDataTypeSpec.dataTypeName,
        List(operation)
      )

      dataType ! message

      awaitAssert(hasBeenTransformed should be(true))
    }

    "ignore duplicated operation messages" in {
      val dataType = system.actorOf(Props(new AbstractServerDataTypeSpecTestServerDataStructure(new HistoryBuffer, DataStructureInstanceId(), new AbstractServerDataTypeSpecTestControlAlgorithm)))
      val operation = AbstractServerDataStructureSpecTestOperation(OperationId(), OperationContext(List.empty), ClientId())
      val message = OperationMessage(
        ClientId(),
        DataStructureInstanceId(),
        AbstractServerDataTypeSpec.dataTypeName,
        List(operation)
      )

      dataType ! message
      dataType ! message

      dataType ! GetHistory
      expectMsgClass(classOf[HistoryBuffer]).history should be(List(operation))
    }

    "publish the transformed operation if a transformation took place" in {
      val dataTypeInstanceId: DataStructureInstanceId = DataStructureInstanceId()
      val clientId: ClientId = ClientId()
      val probe = TestProbe()
      system.eventStream.subscribe(probe.ref, classOf[OperationMessage])
      val originalOperation = AbstractServerDataStructureSpecTestOperation(OperationId(), OperationContext(List.empty), clientId)
      val transformedOperation = AbstractServerDataStructureSpecTestOperation(OperationId(), OperationContext(List(OperationId())), clientId)
      val controlAlgorithm = new ControlAlgorithm {

        override def transform(op: DataStructureOperation, history: HistoryBuffer, transformer: OperationTransformer): DataStructureOperation = transformedOperation

        override def canBeApplied(op: DataStructureOperation, history: HistoryBuffer): Boolean = true

      }
      val dataType = system.actorOf(Props(new AbstractServerDataTypeSpecTestServerDataStructure(new HistoryBuffer, dataTypeInstanceId, controlAlgorithm)))
      val message = OperationMessage(clientId, dataTypeInstanceId, AbstractServerDataTypeSpec.dataTypeName, List(originalOperation))
      dataType ! message

      probe.expectMsg(OperationMessage(message.clientId, message.dataStructureInstanceId, message.dataStructure, List(transformedOperation)))
    }
  }
}

object AbstractServerDataTypeSpec {
  val dataTypeName = DataStructureName("Test")
}
