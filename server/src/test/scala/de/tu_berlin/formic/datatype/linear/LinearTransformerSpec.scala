package de.tu_berlin.formic.datatype.linear

import de.tu_berlin.formic.common.{ClientIdFactory, OperationContext, OperationIdFactory}
import org.scalatest._

/**
  * @author Ronny Bräunlich
  */
class LinearTransformerSpec extends FlatSpec with Matchers {

  "A linear transformer" should "not change the index of the first delete operation if it has a lower index than the second one" in {
    val op1 = LinearDeleteOperation(0, OperationIdFactory(), new OperationContext, ClientIdFactory())
    val op2 = LinearDeleteOperation(1, OperationIdFactory(), new OperationContext, ClientIdFactory())

    val transformed = LinearTransformer.transform(op1, op2)

    transformed should be(op1)
  }

  "A linear transformer" should "decrease the index of the first delete operation by 1 if it has a higher index than the second one" in {
    val op1 = LinearDeleteOperation(2, OperationIdFactory(), new OperationContext, ClientIdFactory())
    val op2 = LinearDeleteOperation(1, OperationIdFactory(), new OperationContext, ClientIdFactory())

    val transformed = LinearTransformer.transform(op1, op2)

    transformed.index should be(1)
    transformed.id should equal(op1.id)
    transformed.operationContext should equal(op1.operationContext)
    transformed.clientId should be(op1.clientId)
  }

  "A linear transformer" should "return a no-op operation if both deletes have same index" in {
    val op1 = LinearDeleteOperation(1, OperationIdFactory(), new OperationContext, ClientIdFactory())
    val op2 = LinearDeleteOperation(1, OperationIdFactory(), new OperationContext, ClientIdFactory())

    val transformed = LinearTransformer.transform(op1, op2)

    transformed.index should equal(-1)
    transformed.id should equal(op1.id)
    transformed.operationContext should equal(op1.operationContext)
    transformed.clientId should be(op1.clientId)
  }

  "A linear transformer" should "not change the index of the first insert operation if it has a lower index than the second one" in {
    val op1 = LinearInsertOperation(0, "A", OperationIdFactory(), new OperationContext, ClientIdFactory())
    val op2 = LinearInsertOperation(1, "B", OperationIdFactory(), new OperationContext, ClientIdFactory())

    val transformed = LinearTransformer.transform(op1, op2)

    transformed should be(op1)
  }

  "A linear transformer" should "increase the index of the first insert operation by one if it has a higher index than the second one" in {
    val op1 = LinearInsertOperation(2, "A", OperationIdFactory(), new OperationContext, ClientIdFactory())
    val op2 = LinearInsertOperation(1, "B", OperationIdFactory(), new OperationContext, ClientIdFactory())

    val transformed = LinearTransformer.transform(op1, op2)

    transformed.index should be(3)
    transformed.id should equal(op1.id)
    transformed.operationContext should equal(op1.operationContext)
    transformed.clientId should be(op1.clientId)
  }

  "A linear transformer" should "return a no-op operation if both insertions want to insert the same object at the same index" in {
    val op1 = LinearInsertOperation(1, "A", OperationIdFactory(), new OperationContext, ClientIdFactory())
    val op2 = LinearInsertOperation(1, "A", OperationIdFactory(), new OperationContext, ClientIdFactory())

    val transformed = LinearTransformer.transform(op1, op2)

    transformed.index should equal(-1)
    transformed.id should equal(op1.id)
    transformed.operationContext should equal(op1.operationContext)
    transformed.clientId should be(op1.clientId)
  }

  "A linear transformer" should "not change the first operation if both insertions want to insert the same object at the same index and it has a higher client id" in {
    val op1 = LinearInsertOperation(1, "A", OperationIdFactory(), new OperationContext, ClientIdFactory("125"))
    val op2 = LinearInsertOperation(1, "B", OperationIdFactory(), new OperationContext, ClientIdFactory("124"))

    val transformed = LinearTransformer.transform(op1, op2)

    transformed should be(op1)
  }

  "A linear transformer" should "increase the index of the first operation if both insertions want to insert the same object at the same index and it has a lower client id" in {
    val op1 = LinearInsertOperation(1, "A", OperationIdFactory(), new OperationContext, ClientIdFactory("123"))
    val op2 = LinearInsertOperation(1, "B", OperationIdFactory(), new OperationContext, ClientIdFactory("124"))

    val transformed = LinearTransformer.transform(op1, op2)

    transformed.index should equal(2)
    transformed.id should equal(op1.id)
    transformed.operationContext should equal(op1.operationContext)
    transformed.clientId should be(op1.clientId)
  }
}
