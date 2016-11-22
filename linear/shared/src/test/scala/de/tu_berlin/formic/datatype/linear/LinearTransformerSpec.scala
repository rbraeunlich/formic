package de.tu_berlin.formic.datatype.linear

import de.tu_berlin.formic.common.datatype.OperationContext
import de.tu_berlin.formic.common.{ClientId, OperationId}
import org.scalatest._

/**
  * @author Ronny Bräunlich
  */
class LinearTransformerSpec extends FlatSpec with Matchers {

  "A linear transformer" should "not change the index of the first delete operation if it has a lower index than the second one" in {
    val op1 = LinearDeleteOperation(0, OperationId(), OperationContext(List.empty), ClientId())
    val op2 = LinearDeleteOperation(1, OperationId(), OperationContext(List.empty), ClientId())

    val transformed = LinearTransformer.transform((op1, op2))

    transformed should equal(LinearDeleteOperation(0, op1.id, OperationContext(List(op2.id)), op1.clientId))
  }

  it should "decrease the index of the first delete operation by 1 if it has a higher index than the second one" in {
    val op1 = LinearDeleteOperation(2, OperationId(), OperationContext(List.empty), ClientId())
    val op2 = LinearDeleteOperation(1, OperationId(), OperationContext(List.empty), ClientId())

    val transformed = LinearTransformer.transform((op1, op2))

    transformed should equal(LinearDeleteOperation(1, op1.id, OperationContext(List(op2.id)), op1.clientId))
  }

  it should "return a no-op operation if both deletes have same index" in {
    val op1 = LinearDeleteOperation(1, OperationId(), OperationContext(List.empty), ClientId())
    val op2 = LinearDeleteOperation(1, OperationId(), OperationContext(List.empty), ClientId())

    val transformed = LinearTransformer.transform((op1, op2))

    transformed should equal(LinearNoOperation(-1, op1.id, OperationContext(List(op2.id)), op1.clientId))
  }

  it should "not change the index of the first insert operation if it has a lower index than the second one" in {
    val op1 = LinearInsertOperation(0, "A", OperationId(), OperationContext(List.empty), ClientId())
    val op2 = LinearInsertOperation(1, "B", OperationId(), OperationContext(List.empty), ClientId())

    val transformed = LinearTransformer.transform((op1, op2))

    transformed should equal(LinearInsertOperation(0, "A", op1.id, OperationContext(List(op2.id)), op1.clientId))
  }

  it should "increase the index of the first insert operation by one if it has a higher index than the second one" in {
    val op1 = LinearInsertOperation(2, "A", OperationId(), OperationContext(List.empty), ClientId())
    val op2 = LinearInsertOperation(1, "B", OperationId(), OperationContext(List.empty), ClientId())

    val transformed = LinearTransformer.transform((op1, op2))

    transformed should equal(LinearInsertOperation(3, "A", op1.id, OperationContext(List(op2.id)), op1.clientId))
  }

  it should "return a no-op operation if both insertions want to insert the same object at the same index" in {
    val op1 = LinearInsertOperation(1, "A", OperationId(), OperationContext(List.empty), ClientId())
    val op2 = LinearInsertOperation(1, "A", OperationId(), OperationContext(List.empty), ClientId())

    val transformed = LinearTransformer.transform((op1, op2))

    transformed should equal(LinearNoOperation(-1, op1.id, OperationContext(List(op2.id)), op1.clientId))
  }

  it should "not change the first operation if both insertions want to insert the same object at the same index and it has a higher client id" in {
    val op1 = LinearInsertOperation(1, "A", OperationId(), OperationContext(List.empty), ClientId("125"))
    val op2 = LinearInsertOperation(1, "B", OperationId(), OperationContext(List.empty), ClientId("124"))

    val transformed = LinearTransformer.transform((op1, op2))

    transformed should equal(LinearInsertOperation(1, "A", op1.id, OperationContext(List(op2.id)), op1.clientId))
  }

  it should "increase the index of the first operation if both insertions want to insert the same object at the same index and it has a lower client id" in {
    val op1 = LinearInsertOperation(1, "A", OperationId(), OperationContext(List.empty), ClientId("123"))
    val op2 = LinearInsertOperation(1, "B", OperationId(), OperationContext(List.empty), ClientId("124"))

    val transformed = LinearTransformer.transform((op1, op2))

    transformed should equal(LinearInsertOperation(2, "A", op1.id, OperationContext(List(op2.id)), op1.clientId))
  }

  it should "not change the first insert operation if it has a lower index than the delete operation" in {
    val op1 = LinearInsertOperation(1, "A", OperationId(), OperationContext(List.empty), ClientId("125"))
    val op2 = LinearDeleteOperation(2, OperationId(), OperationContext(List.empty), ClientId("124"))

    val transformed = LinearTransformer.transform((op1, op2))

    transformed should equal(LinearInsertOperation(1, "A", op1.id, OperationContext(List(op2.id)), op1.clientId))
  }

  it should "decrease the index of the first insert operation by one if it has the same index like the delete operation" in {
    val op1 = LinearInsertOperation(1, "A", OperationId(), OperationContext(List.empty), ClientId("125"))
    val op2 = LinearDeleteOperation(1, OperationId(), OperationContext(List.empty), ClientId("124"))

    val transformed = LinearTransformer.transform((op1, op2))

    transformed should equal(LinearInsertOperation(0, "A", op1.id, OperationContext(List(op2.id)), op1.clientId))
  }

  it should "decrease the index of the first insert operation by one if it has a higher index than the delete operation" in {
    val op1 = LinearInsertOperation(2, "A", OperationId(), OperationContext(List.empty), ClientId("125"))
    val op2 = LinearDeleteOperation(1, OperationId(), OperationContext(List.empty), ClientId("124"))

    val transformed = LinearTransformer.transform((op1, op2))

    transformed should equal(LinearInsertOperation(1, "A", op1.id, OperationContext(List(op2.id)), op1.clientId))
  }

  it should "not change the first delete operation if it has a lower index than the insert operation" in {
    val op1 = LinearDeleteOperation(1, OperationId(), OperationContext(List.empty), ClientId("124"))
    val op2 = LinearInsertOperation(2, "A", OperationId(), OperationContext(List.empty), ClientId("125"))

    val transformed = LinearTransformer.transform((op1, op2))

    transformed should equal(LinearDeleteOperation(1, op1.id, OperationContext(List(op2.id)), op1.clientId))
  }

  it should "not change the first delete operation if it has the same index than the insert operation" in {
    val op1 = LinearDeleteOperation(1, OperationId(), OperationContext(List.empty), ClientId("124"))
    val op2 = LinearInsertOperation(1, "A", OperationId(), OperationContext(List.empty), ClientId("125"))

    val transformed = LinearTransformer.transform((op1, op2))

    transformed should equal(LinearDeleteOperation(1, op1.id, OperationContext(List(op2.id)), op1.clientId))
  }

  it should "increase the index of the first delete operation if it has a higher index than the insert operation" in {
    val op1 = LinearDeleteOperation(2, OperationId(), OperationContext(List.empty), ClientId("124"))
    val op2 = LinearInsertOperation(1, "A", OperationId(), OperationContext(List.empty), ClientId("125"))

    val transformed = LinearTransformer.transform((op1, op2))

    transformed should equal(LinearDeleteOperation(3, op1.id, OperationContext(List(op2.id)), op1.clientId))
  }
}
