package de.tu_berlin.formic.common.datatype

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import de.tu_berlin.formic.common.message.{HistoricOperationRequest, OperationMessage, UpdateRequest, UpdateResponse}
import de.tu_berlin.formic.common.{ClientId, DataTypeInstanceId, OperationId}
import org.scalatest.Assertions._
import org.scalatest.{Matchers, WordSpecLike}

class AbstractDataTypeSpecTestDataType(override val historyBuffer: HistoryBuffer) extends AbstractDataType {

  var data = "{data}"

  override def apply(op: DataTypeOperation): Unit = {
    op match {
      case test: AbstractDataTypeSpecTestOperation => data = "{received}"
      case _ => fail
    }
  }

  override val dataTypeName: DataTypeName = AbstractDataTypeSpec.dataTypeName

  override def getDataAsJson: String = data
}

case class AbstractDataTypeSpecTestOperation(id: OperationId, operationContext: OperationContext, clientId: ClientId) extends DataTypeOperation

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

      system.actorOf(Props(new AbstractDataTypeSpecTestDataType(new HistoryBuffer)))

      val msg = probe.expectMsgClass(classOf[UpdateResponse])
      msg.dataType should be(AbstractDataTypeSpec.dataTypeName)
      msg.data should be("{data}")
    }

    "apply received operations from an operation message and publish the same message again" in {
      val probe = TestProbe()
      system.eventStream.subscribe(probe.ref, classOf[OperationMessage])
      val dataType = system.actorOf(Props(new AbstractDataTypeSpecTestDataType(new HistoryBuffer)))
      val message = OperationMessage(
        ClientId(),
        DataTypeInstanceId(),
        AbstractDataTypeSpec.dataTypeName,
        List(AbstractDataTypeSpecTestOperation(OperationId(), OperationContext(List.empty), ClientId()))
      )
      dataType ! message

      probe.expectMsg(message)
    }

    "return the requested historic operations when receiving HistoricOperationsRequest" in {
      val probe = TestProbe()
      system.eventStream.subscribe(probe.ref, classOf[UpdateResponse])

      val op1 = AbstractDataTypeSpecTestOperation(OperationId(), OperationContext(List.empty), ClientId())
      val op2 = AbstractDataTypeSpecTestOperation(OperationId(), OperationContext(List(op1.id)), ClientId())
      val historyBuffer = new HistoryBuffer
      historyBuffer.addOperation(op1)
      historyBuffer.addOperation(op2)
      val clientId = ClientId()
      val dataType = system.actorOf(Props(new AbstractDataTypeSpecTestDataType(historyBuffer)))
      val msg = probe.expectMsgClass(classOf[UpdateResponse])
      val dataTypeInstanceId = msg.dataTypeInstanceId

      dataType ! HistoricOperationRequest(clientId, dataTypeInstanceId, op1.id)

      expectMsg(OperationMessage(clientId, dataTypeInstanceId, AbstractDataTypeSpec.dataTypeName, List(op2)))
    }

    "answer an UpdateRequest with its full data in an UpdateResponse" in {
      val probe = TestProbe()
      system.eventStream.subscribe(probe.ref, classOf[UpdateResponse])
      val clientId = ClientId()
      val dataType = system.actorOf(Props(new AbstractDataTypeSpecTestDataType(new HistoryBuffer)))
      val msg = probe.expectMsgClass(classOf[UpdateResponse])
      val dataTypeInstanceId = msg.dataTypeInstanceId

      dataType ! UpdateRequest(clientId, dataTypeInstanceId)

      expectMsg(UpdateResponse(dataTypeInstanceId, AbstractDataTypeSpec.dataTypeName, "{data}"))
    }
  }
}

object AbstractDataTypeSpec {
  val dataTypeName = DataTypeName("Test")
}
