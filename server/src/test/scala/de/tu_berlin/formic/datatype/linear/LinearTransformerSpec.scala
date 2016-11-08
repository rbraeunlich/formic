package de.tu_berlin.formic.datatype.linear

import de.tu_berlin.formic.common.{OperationContext, OperationIdFactory}
import org.scalatest._

/**
  * @author Ronny Bräunlich
  */
class LinearTransformerSpec extends FlatSpec with Matchers {

  "A transformer" should "not change the index of the first delete operation if it has a lower index than the second one" in {
    val op1 = LinearDeleteOperation(0, OperationIdFactory(), new OperationContext)
    val op2 = LinearDeleteOperation(1, OperationIdFactory(), new OperationContext)

    val transformed = LinearTransformer.transform(op1, op2)

    transformed should be(op1)
  }

  "A transformer" should "decrease the index of the first delete operation by 1 if it has a higher index than the second one" in {
    val op1 = LinearDeleteOperation(2, OperationIdFactory(), new OperationContext)
    val op2 = LinearDeleteOperation(1, OperationIdFactory(), new OperationContext)

    val transformed = LinearTransformer.transform(op1, op2)

    transformed.index should be(1)
    transformed.id should equal (op1.id)
    transformed.operationContext should equal (op1.operationContext)
  }

  "A transformer" should "return no-op operation if both deletes have same index" in {
    val op1 = LinearDeleteOperation(1, OperationIdFactory(), new OperationContext)
    val op2 = LinearDeleteOperation(1, OperationIdFactory(), new OperationContext)

    val transformed = LinearTransformer.transform(op1, op2)

    transformed.index should equal (-1)
    transformed.id should equal (op1.id)
    transformed.operationContext should equal (op1.operationContext)
  }

}
