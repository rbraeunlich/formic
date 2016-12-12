package de.tu_berlin.formic.common.datatype

import de.tu_berlin.formic.common.{ClientId, OperationId}
import org.scalatest.{FlatSpec, Matchers}

/**
  * @author Ronny Bräunlich
  */
class HistoryBufferSpec extends FlatSpec with Matchers {

  case class TestDataTypeOperation(id: OperationId, operationContext: OperationContext,var clientId: ClientId) extends DataTypeOperation

  "The History buffer" should "place the newest operations in front of the history" in {
    val buffer = new HistoryBuffer()
    val clientId = ClientId("1")
    val op1 = TestDataTypeOperation(OperationId("1"), OperationContext(List.empty), clientId)
    val op2 = TestDataTypeOperation(OperationId("2"), OperationContext(List(op1.id)), clientId)
    val op3 = TestDataTypeOperation(OperationId("3"), OperationContext(List(op2.id)), clientId)

    buffer.addOperation(op1)
    buffer.addOperation(op2)
    buffer.addOperation(op3)

    buffer.history should be(List(op3, op2, op1))
  }

  it should "find an operation in the history" in {
    val buffer = new HistoryBuffer()
    val clientId = ClientId("1")
    val op1 = TestDataTypeOperation(OperationId("1"), OperationContext(List.empty), clientId)
    val op2 = TestDataTypeOperation(OperationId("2"), OperationContext(List(op1.id)), clientId)

    buffer.addOperation(op1)
    buffer.addOperation(op2)

    buffer.findOperation(op1.id) should be(Some(op1))
    buffer.findOperation(op2.id) should be(Some(op2))
  }

  it should "return an empty option for unknown operation" in {
    val buffer = new HistoryBuffer()

    buffer.findOperation(OperationId("123")) should be(None)
  }

  it should "return an empty list if no later operations are present" in {
    val buffer = new HistoryBuffer()
    val op1 = TestDataTypeOperation(OperationId("1"), OperationContext(List.empty), ClientId("1"))
    buffer.addOperation(op1)

    buffer.findAllOperationsAfter(op1.id) should be(empty)
  }

  it should "return an empty list of the id is unkown for previous operations" in {
    val buffer = new HistoryBuffer()
    val op1 = TestDataTypeOperation(OperationId("1"), OperationContext(List.empty), ClientId("1"))
    buffer.addOperation(op1)

    buffer.findAllOperationsAfter(OperationId("5")) should be(empty)
  }

  it should "return all operations for null as previous operation id" in {
    val buffer = new HistoryBuffer()
    val op1 = TestDataTypeOperation(OperationId("1"), OperationContext(List.empty), ClientId("1"))
    buffer.addOperation(op1)

    buffer.findAllOperationsAfter(null) should contain(op1)
  }

  it should "find all later operations in correct order" in {
    val buffer = new HistoryBuffer()
    val clientId = ClientId("1")
    val op1 = TestDataTypeOperation(OperationId("1"), OperationContext(List.empty), clientId)
    val op2 = TestDataTypeOperation(OperationId("2"), OperationContext(List(op1.id)), clientId)
    val op3 = TestDataTypeOperation(OperationId("3"), OperationContext(List(op2.id)), clientId)

    buffer.addOperation(op1)
    buffer.addOperation(op2)
    buffer.addOperation(op3)

    buffer.findAllOperationsAfter(op1.id) should be(List(op3,op2))
    buffer.findAllOperationsAfter(op2.id) should be(List(op3))
  }


}
