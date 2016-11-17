package de.tu_berlin.formic.common.datatype

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import de.tu_berlin.formic.common.message.{HistoricOperationRequest, OperationMessage, UpdateRequest, UpdateResponse}
import de.tu_berlin.formic.common.{ClientId, DataTypeInstanceId, OperationId}
import org.scalatest.Assertions._
import org.scalatest.{Matchers, WordSpecLike}

class TestDataType(override val historyBuffer: HistoryBuffer) extends AbstractDataType {

  var data = "{data}"

  override def apply(op: DataTypeOperation): Unit = {
    op match {
      case test: TestOperation => data = "{received}"
      case _ => fail
    }
  }

  override val dataTypeName: DataTypeName = DataTypeName("Test")

  override def getDataAsJson: String = data
}

case class TestOperation(id: OperationId, operationContext: OperationContext, clientId: ClientId) extends DataTypeOperation

/**
  * @author Ronny Bräunlich
  */
class AbstractDataTypeSpec extends TestKit(ActorSystem("testsystem"))
  with WordSpecLike
  with ImplicitSender
  with StopSystemAfterAll
  with Matchers {

  "An AbstractDataType" must {

    "publish an UpdateResponse after creation" in {
      val probe = TestProbe()
      system.eventStream.subscribe(probe.ref, classOf[UpdateResponse])

      system.actorOf(Props(new TestDataType(new HistoryBuffer)))

      val msg = probe.expectMsgClass(classOf[UpdateResponse])
      msg.dataType should be(DataTypeName("Test"))
      msg.data should be("{data}")
    }

    "apply received operations from an operation message" in {
      val dataType = system.actorOf(Props(new TestDataType(new HistoryBuffer)))
      dataType ! OperationMessage(
        ClientId(),
        DataTypeInstanceId(),
        DataTypeName("Test"),
        List(TestOperation(OperationId(), OperationContext(List.empty), ClientId()))
      )

      val msg = expectMsgClass(classOf[UpdateResponse])
      msg.dataType should be(DataTypeName("Test"))
      msg.data should be("{received}")
    }

    "return the requested historic operations when receiving HistoricOperationsRequest" in {
      val probe = TestProbe()
      system.eventStream.subscribe(probe.ref, classOf[UpdateResponse])

      val op1 = TestOperation(OperationId(), OperationContext(List.empty), ClientId())
      val op2 = TestOperation(OperationId(), OperationContext(List(op1.id)), ClientId())
      val historyBuffer = new HistoryBuffer
      historyBuffer.addOperation(op1)
      historyBuffer.addOperation(op2)
      val clientId = ClientId()
      val dataType = system.actorOf(Props(new TestDataType(historyBuffer)))
      val msg = probe.expectMsgClass(classOf[UpdateResponse])
      val dataTypeInstanceId = msg.dataTypeInstanceId

      dataType ! HistoricOperationRequest(clientId, dataTypeInstanceId, op1.id)

      expectMsg(OperationMessage(clientId, dataTypeInstanceId, DataTypeName("Test"), List(op2)))
    }

    "answer an UpdateRequest with its full data in an UpdateResponse" in {
      val probe = TestProbe()
      system.eventStream.subscribe(probe.ref, classOf[UpdateResponse])
      val clientId = ClientId()
      val dataType = system.actorOf(Props(new TestDataType(new HistoryBuffer)))
      val msg = probe.expectMsgClass(classOf[UpdateResponse])
      val dataTypeInstanceId = msg.dataTypeInstanceId

      dataType ! UpdateRequest(clientId, dataTypeInstanceId)

      expectMsg(UpdateResponse(dataTypeInstanceId, DataTypeName("Test"), "{data}"))
    }
  }

}
