package de.tu_berlin.formic.datatype.linear

import de.tu_berlin.formic.common.OperationIdFactory
import org.scalatest._

/**
  * @author Ronny Bräunlich
  */
class LinearTransformerSpec extends FlatSpec with Matchers {

  "A transformer" should "not change the index of the first delete operation if it has a lower index than the second one" in {
    val op1 = LinearDeleteOperation(0, OperationIdFactory())
    val op2 = LinearDeleteOperation(1, OperationIdFactory())

    val transformed = LinearTransformer.transform(op1, op2)

    transformed.index should be(0)
  }

  "A transformer" should "decrease the index of the first delete operation by 1 if it has a higher index than the second one" in {
    val op1 = LinearDeleteOperation(2, OperationIdFactory())
    val op2 = LinearDeleteOperation(1, OperationIdFactory())

    val transformed = LinearTransformer.transform(op1, op2)

    transformed.index should be(1)
  }

}
