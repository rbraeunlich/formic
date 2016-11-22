package de.tu_berlin.formic.datatype.linear.server

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestActorRef, TestKit}
import de.tu_berlin.formic.common.controlalgo.ControlAlgorithm
import de.tu_berlin.formic.common.datatype.{DataTypeOperation, HistoryBuffer, OperationContext, OperationTransformer}
import de.tu_berlin.formic.common.message.OperationMessage
import de.tu_berlin.formic.common.{ClientId, DataTypeInstanceId, OperationId}
import de.tu_berlin.formic.datatype.linear.{LinearDataType, LinearDeleteOperation, LinearInsertOperation}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.collection.mutable.ArrayBuffer

/**
  * @author Ronny Bräunlich
  */
class LinearDataTypeSpec extends TestKit(ActorSystem("testsystem"))
  with WordSpecLike
  with ImplicitSender
  with Matchers
  with BeforeAndAfterAll {

  "LinearDataType" must {

    "insert data" in {
      val dataType = TestActorRef[LinearDataType[Int]](Props(LinearDataType[Int](DataTypeInstanceId(), LinearDataTypeSpecControlAlgorithm)))
      val op = LinearInsertOperation(0, Integer.valueOf(1), OperationId(), OperationContext(List.empty), ClientId())
      val op2 = LinearInsertOperation(1, Integer.valueOf(3), OperationId(), OperationContext(List.empty), ClientId())
      val op3 = LinearInsertOperation(0, Integer.valueOf(0), OperationId(), OperationContext(List.empty), ClientId())

      dataType ! OperationMessage(ClientId(), DataTypeInstanceId(), LinearDataType.dataTypeName, List(op))
      dataType ! OperationMessage(ClientId(), DataTypeInstanceId(), LinearDataType.dataTypeName, List(op2))
      dataType ! OperationMessage(ClientId(), DataTypeInstanceId(), LinearDataType.dataTypeName, List(op3))

      dataType.underlyingActor.data should be(ArrayBuffer(0, 1, 3))
    }

    "delete data" in {
      val dataType = TestActorRef[LinearDataType[Int]](Props(LinearDataType[Int](DataTypeInstanceId(), LinearDataTypeSpecControlAlgorithm)))
      val op = LinearInsertOperation(0, Integer.valueOf(1), OperationId(), OperationContext(List.empty), ClientId())
      val op2 = LinearInsertOperation(1, Integer.valueOf(3), OperationId(), OperationContext(List.empty), ClientId())
      val op3 = LinearInsertOperation(0, Integer.valueOf(0), OperationId(), OperationContext(List.empty), ClientId())

      //operations have to be reversed!
      dataType ! OperationMessage(ClientId(), DataTypeInstanceId(), LinearDataType.dataTypeName, List(op3, op2, op))

      val delete = LinearDeleteOperation(1, OperationId(), OperationContext(List.empty), ClientId())

      dataType ! OperationMessage(ClientId(), DataTypeInstanceId(), LinearDataType.dataTypeName, List(delete))

      dataType.underlyingActor.data should be(ArrayBuffer(0, 3))
    }

    "result in a valid JSON representation" in {
      val dataType = TestActorRef[LinearDataType[Int]](Props(LinearDataType[Int](DataTypeInstanceId(), LinearDataTypeSpecControlAlgorithm)))
      val op = LinearInsertOperation(0, Integer.valueOf(1), OperationId(), OperationContext(List.empty), ClientId())
      val op2 = LinearInsertOperation(1, Integer.valueOf(3), OperationId(), OperationContext(List.empty), ClientId())
      val op3 = LinearInsertOperation(0, Integer.valueOf(0), OperationId(), OperationContext(List.empty), ClientId())

      //operations have to be reversed!
      dataType ! OperationMessage(ClientId(), DataTypeInstanceId(), LinearDataType.dataTypeName, List(op3, op2, op))

      dataType.underlyingActor.getDataAsJson() should equal("[0,1,3]")
    }

  }
}
object LinearDataTypeSpecControlAlgorithm extends ControlAlgorithm {

  override def canBeApplied(op: DataTypeOperation, history: HistoryBuffer): Boolean = true

  override def transform(op: DataTypeOperation, history: HistoryBuffer, transformer: OperationTransformer): DataTypeOperation = op
}