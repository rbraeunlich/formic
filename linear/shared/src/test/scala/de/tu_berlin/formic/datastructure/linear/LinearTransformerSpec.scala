package de.tu_berlin.formic.datastructure.linear

import de.tu_berlin.formic.common.controlalgo.WaveOTClient
import de.tu_berlin.formic.common.datastructure.{DataStructureOperation, HistoryBuffer, OperationContext}
import de.tu_berlin.formic.common.{ClientId, OperationId}
import org.scalatest._
import upickle.default._

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

    transformed should equal(LinearNoOperation(op1.id, OperationContext(List(op2.id)), op1.clientId))
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

    transformed should equal(LinearNoOperation(op1.id, OperationContext(List(op2.id)), op1.clientId))
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

  it should "increase the index of the first delete operation if it has the same index than the insert operation" in {
    val op1 = LinearDeleteOperation(1, OperationId(), OperationContext(List.empty), ClientId("124"))
    val op2 = LinearInsertOperation(1, "A", OperationId(), OperationContext(List.empty), ClientId("125"))

    val transformed = LinearTransformer.transform((op1, op2))

    transformed should equal(LinearDeleteOperation(2, op1.id, OperationContext(List(op2.id)), op1.clientId))
  }

  it should "increase the index of the first delete operation if it has a higher index than the insert operation" in {
    val op1 = LinearDeleteOperation(2, OperationId(), OperationContext(List.empty), ClientId("124"))
    val op2 = LinearInsertOperation(1, "A", OperationId(), OperationContext(List.empty), ClientId("125"))

    val transformed = LinearTransformer.transform((op1, op2))

    transformed should equal(LinearDeleteOperation(3, op1.id, OperationContext(List(op2.id)), op1.clientId))
  }

  it should "not bulk transform anything if the bridge is empty" in {
    val op = LinearDeleteOperation(2, OperationId(), OperationContext(List.empty), ClientId("124"))

    val transformed = LinearTransformer.bulkTransform(op, List.empty)

    transformed shouldBe empty
  }

  it should "bulk transform complete bridge and change context of first transformed operation" in {
    val op = LinearDeleteOperation(1, OperationId(), OperationContext(List.empty), ClientId("124"))
    val op1 = LinearDeleteOperation(2, OperationId(), OperationContext(List.empty), ClientId())
    val op2 = LinearDeleteOperation(3, OperationId(), OperationContext(List(op1.id)), ClientId())

    val transformed = LinearTransformer.bulkTransform(op, List(op2, op1))

    transformed should contain inOrder(
      LinearDeleteOperation(2, op2.id, op2.operationContext, op2.clientId),
      LinearDeleteOperation(1, op1.id, OperationContext(List(op.id)), op1.clientId))
  }

  it should "bulk transform complete bridge against the continuously transformed operations" in {
    val op = LinearDeleteOperation(1, OperationId(), OperationContext(List.empty), ClientId("124"))
    val op1 = LinearInsertOperation(0, "a", OperationId(), OperationContext(List.empty), ClientId())
    val op2 = LinearInsertOperation(2, "b", OperationId(), OperationContext(List(op1.id)), ClientId())
    val op3 = LinearInsertOperation(2, "c", OperationId(), OperationContext(List(op1.id)), ClientId())

    val transformed = LinearTransformer.bulkTransform(op, List(op3, op2, op1))

    transformed should contain inOrder(
      LinearInsertOperation(2, "c", op3.id, op3.operationContext, op3.clientId),
      LinearInsertOperation(2, "b", op2.id, op2.operationContext, op2.clientId),
      LinearInsertOperation(0, "a", op1.id, OperationContext(List(op.id)), op1.clientId))
  }

  it should "not change the insert operation when it has the same index as the delete operation" in {
    val op1 = LinearInsertOperation(0, "a", OperationId(), OperationContext(), ClientId())
    val op2 = LinearDeleteOperation(0, OperationId(), OperationContext(), ClientId())

    val transformed = LinearTransformer.transform((op1, op2))

    transformed should equal(LinearInsertOperation(op1.index, op1.o, op1.id, OperationContext(List(op2.id)), op1.clientId))
  }

  it should "not change an insert when its transformed against a no-op" in {
    val op1 = LinearInsertOperation(0, "a", OperationId(), OperationContext(), ClientId())
    val op2 = LinearNoOperation(OperationId(), OperationContext(), ClientId())

    val transformed = LinearTransformer.transform(op1, op2)

    transformed should equal(LinearInsertOperation(op1.index, op1.o, op1.id, OperationContext(List(op2.id)), op1.clientId))
  }

  it should "not change a delete when its transformed against a no-op" in {
    val op1 = LinearDeleteOperation(0, OperationId(), OperationContext(), ClientId())
    val op2 = LinearNoOperation(OperationId(), OperationContext(), ClientId())

    val transformed = LinearTransformer.transform(op1, op2)

    transformed should equal(LinearDeleteOperation(op1.index, op1.id, OperationContext(List(op2.id)), op1.clientId))
  }

  it should "not change a no-op when its transformed against an operation" in {
    val op1 = LinearNoOperation(OperationId(), OperationContext(), ClientId())
    val op2 = LinearInsertOperation(0, "a", OperationId(), OperationContext(), ClientId())

    val transformed = LinearTransformer.transform(op1, op2)

    transformed should equal(LinearNoOperation(op1.id, OperationContext(List(op2.id)), op1.clientId))
  }

  it should "bulk transform bridge with two operations in it correctly" in {
    val op1 = LinearInsertOperation(0, "z", OperationId(), OperationContext(), ClientId("2"))
    val op2 = LinearInsertOperation(1, "z", OperationId(), OperationContext(List(op1.id)), ClientId("2"))
    val op3 = LinearInsertOperation(2, "z", OperationId(), OperationContext(List(op2.id)), ClientId("2"))
    val op = LinearInsertOperation(0, "a", OperationId(), OperationContext(), ClientId("1"))

    val transformed = LinearTransformer.bulkTransform(op, List(op3, op2, op1))

    transformed should contain inOrder(
      LinearInsertOperation(op3.index, op3.o, op3.id, op3.operationContext, op3.clientId),
      LinearInsertOperation(op2.index, op2.o, op2.id, op2.operationContext, op2.clientId),
      LinearInsertOperation(op1.index, op1.o, op1.id, OperationContext(List(op.id)), op1.clientId))
  }

  it should "correctly increase the index in the bridge when bulk transforming" in {
    val op1 = LinearInsertOperation(0, "z", OperationId(), OperationContext(), ClientId("1"))
    val op2 = LinearInsertOperation(1, "z", OperationId(), OperationContext(List(op1.id)), ClientId("1"))
    val op3 = LinearInsertOperation(2, "z", OperationId(), OperationContext(List(op2.id)), ClientId("1"))
    val op = LinearInsertOperation(0, "a", OperationId(), OperationContext(), ClientId("2"))

    val transformed = LinearTransformer.bulkTransform(op, List(op3, op2, op1))

    transformed should contain inOrder(
      LinearInsertOperation(op3.index + 1, op3.o, op3.id, op3.operationContext, op3.clientId),
      LinearInsertOperation(op2.index + 1, op2.o, op2.id, op2.operationContext, op2.clientId),
      LinearInsertOperation(op1.index + 1, op1.o, op1.id, OperationContext(List(op.id)), op1.clientId))
  }

  it should "not change the bridge when bulk transforming against a noop" in {
    val op1 = LinearInsertOperation(0, "z", OperationId(), OperationContext(), ClientId("1"))
    val op2 = LinearInsertOperation(1, "z", OperationId(), OperationContext(List(op1.id)), ClientId("1"))
    val op3 = LinearInsertOperation(2, "z", OperationId(), OperationContext(List(op2.id)), ClientId("1"))
    val op = LinearNoOperation(OperationId(), OperationContext(), ClientId("2"))

    val transformed = LinearTransformer.bulkTransform(op, List(op3, op2, op1))

    transformed should contain inOrder(
      LinearInsertOperation(op3.index, op3.o, op3.id, op3.operationContext, op3.clientId),
      LinearInsertOperation(op2.index, op2.o, op2.id, op2.operationContext, op2.clientId),
      LinearInsertOperation(op1.index, op1.o, op1.id, OperationContext(List(op.id)), op1.clientId))
  }

  it should "correctly perform bulk transformations with more than one operations in the bridge with WaveOTClient" in {
    val controlAlgo = new WaveOTClient(_ => {})
    val op1 = LinearInsertOperation(0, "z", OperationId(), OperationContext(), ClientId("2"))
    val op2 = LinearInsertOperation(1, "z", OperationId(), OperationContext(List(op1.id)), ClientId("2"))
    val op3 = LinearInsertOperation(2, "z", OperationId(), OperationContext(List(op2.id)), ClientId("2"))
    val op4 = LinearInsertOperation(3, "z", OperationId(), OperationContext(List(op3.id)), ClientId("2"))
    val op = LinearInsertOperation(0, "a", OperationId(), OperationContext(), ClientId("1"))
    controlAlgo.canLocalOperationBeApplied(op1)
    controlAlgo.canLocalOperationBeApplied(op2)
    controlAlgo.canLocalOperationBeApplied(op3)
    controlAlgo.canLocalOperationBeApplied(op4)

    val transformed = controlAlgo.transform(op, new HistoryBuffer(), LinearTransformer)

    transformed should equal(LinearInsertOperation(4, op.o, op.id, OperationContext(List(op4.id)), op.clientId))
    controlAlgo.inFlightOperation should equal(LinearInsertOperation(op1.index, op1.o, op1.id, OperationContext(List(op.id)), op1.clientId))
    controlAlgo.buffer should contain inOrder(
      LinearInsertOperation(op2.index, op2.o, op2.id, op2.operationContext, op2.clientId),
      LinearInsertOperation(op3.index, op3.o, op3.id, op3.operationContext, op3.clientId),
      LinearInsertOperation(op4.index, op4.o, op4.id, op4.operationContext, op4.clientId))
  }

  it should "correctly bulk transformation the incoming operation to a noop with WaveOTClient" in {
    val controlAlgo = new WaveOTClient(_ => {})
    val op1 = LinearInsertOperation(0, "z", OperationId(), OperationContext(), ClientId("2"))
    val op2 = LinearInsertOperation(1, "z", OperationId(), OperationContext(List(op1.id)), ClientId("2"))
    val op3 = LinearInsertOperation(2, "z", OperationId(), OperationContext(List(op2.id)), ClientId("2"))
    val op = LinearInsertOperation(0, "z", OperationId(), OperationContext(), ClientId("1"))
    controlAlgo.canLocalOperationBeApplied(op1)
    controlAlgo.canLocalOperationBeApplied(op2)
    controlAlgo.canLocalOperationBeApplied(op3)

    val transformed = controlAlgo.transform(op, new HistoryBuffer(), LinearTransformer)

    transformed should equal(LinearNoOperation(op.id, OperationContext(List(op3.id)), op.clientId))
    controlAlgo.inFlightOperation should equal(LinearNoOperation(op1.id, OperationContext(List(op.id)), op1.clientId))
    controlAlgo.buffer should contain inOrder(
      LinearInsertOperation(op2.index, op2.o, op2.id, op2.operationContext, op2.clientId),
      LinearInsertOperation(op3.index, op3.o, op3.id, op3.operationContext, op3.clientId))
  }

  it should "correctly bulk transform the first operation in the bridge to a noop with WaveOTClient" in {
    val controlAlgo = new WaveOTClient(_ => {})
    val op1 = LinearInsertOperation(0, "z", OperationId(), OperationContext(), ClientId("2"))
    val op2 = LinearInsertOperation(1, "z", OperationId(), OperationContext(List(op1.id)), ClientId("2"))
    val op3 = LinearInsertOperation(2, "z", OperationId(), OperationContext(List(op2.id)), ClientId("2"))
    val op = LinearInsertOperation(0, "z", OperationId(), OperationContext(), ClientId("3"))
    controlAlgo.canLocalOperationBeApplied(op1)
    controlAlgo.canLocalOperationBeApplied(op2)
    controlAlgo.canLocalOperationBeApplied(op3)

    val transformed = controlAlgo.transform(op, new HistoryBuffer(), LinearTransformer)

    transformed should equal(LinearNoOperation(op.id, OperationContext(List(op3.id)), op.clientId))
    controlAlgo.inFlightOperation should equal(LinearNoOperation(op1.id, OperationContext(List(op.id)), op1.clientId))
    controlAlgo.buffer should contain inOrder(
      LinearInsertOperation(op2.index, op2.o, op2.id, op2.operationContext, op2.clientId),
      LinearInsertOperation(op3.index, op3.o, op3.id, op3.operationContext, op3.clientId))
  }

  it should "correctly bulk transform a middle operation in the bridge to a noop with WaveOTClient" in {
    val controlAlgo = new WaveOTClient(_ => {})
    val op1 = LinearInsertOperation(10, "z", OperationId(), OperationContext(), ClientId("2"))
    val op2 = LinearInsertOperation(1, "z", OperationId(), OperationContext(List(op1.id)), ClientId("2"))
    val op3 = LinearInsertOperation(2, "z", OperationId(), OperationContext(List(op2.id)), ClientId("2"))
    val op = LinearInsertOperation(1, "z", OperationId(), OperationContext(), ClientId("3"))
    controlAlgo.canLocalOperationBeApplied(op1)
    controlAlgo.canLocalOperationBeApplied(op2)
    controlAlgo.canLocalOperationBeApplied(op3)

    val transformed = controlAlgo.transform(op, new HistoryBuffer(), LinearTransformer)

    transformed should equal(LinearNoOperation(op.id, OperationContext(List(op3.id)), op.clientId))
    controlAlgo.inFlightOperation should equal(LinearInsertOperation(11, op1.o, op1.id, OperationContext(List(op.id)), op1.clientId))
    controlAlgo.buffer should contain inOrder(
      LinearNoOperation(op2.id, op2.operationContext, op2.clientId),
      LinearInsertOperation(op3.index, op3.o, op3.id, op3.operationContext, op3.clientId))
  }

  it should "not transform the whole bridge into noops when all operations in the bridge are the same" in {
    val op1 = LinearInsertOperation(1, "z", OperationId(), OperationContext(), ClientId("2"))
    val op2 = LinearInsertOperation(1, "z", OperationId(), OperationContext(List(op1.id)), ClientId("2"))
    val op3 = LinearInsertOperation(1, "z", OperationId(), OperationContext(List(op2.id)), ClientId("2"))
    val op = LinearInsertOperation(1, "z", OperationId(), OperationContext(), ClientId("3"))

    val transformed = LinearTransformer.bulkTransform(op, List(op3, op2, op1))

    transformed should contain inOrder(
      LinearInsertOperation(op3.index, op3.o, op3.id, op3.operationContext, op3.clientId),
      LinearInsertOperation(op2.index, op2.o, op2.id, op2.operationContext, op2.clientId),
      LinearNoOperation(op1.id, OperationContext(List(op.id)), op1.clientId))
  }

  it should "make send WaveOTClient a noop to the server after receiving an ack for the inFlightOperation" in {
    var sentOperations: List[DataStructureOperation] = List.empty
    val controlAlgo = new WaveOTClient(op => {sentOperations = sentOperations :+ op})
    val op1 = LinearInsertOperation(10, "z", OperationId(), OperationContext(), ClientId("2"))
    val op2 = LinearInsertOperation(1, "z", OperationId(), OperationContext(List(op1.id)), ClientId("2"))
    val op3 = LinearInsertOperation(2, "z", OperationId(), OperationContext(List(op2.id)), ClientId("2"))
    val op = LinearInsertOperation(1, "z", OperationId(), OperationContext(), ClientId("3"))
    controlAlgo.canLocalOperationBeApplied(op1)
    controlAlgo.canLocalOperationBeApplied(op2)
    controlAlgo.canLocalOperationBeApplied(op3)

    controlAlgo.transform(op, new HistoryBuffer(), LinearTransformer)
    controlAlgo.canBeApplied(op1, new HistoryBuffer())

    sentOperations should contain inOrder(
      op1,
      LinearNoOperation(op2.id, op2.operationContext, op2.clientId))
  }

  it should "throw an exception when both insertions contain incompatible objects" in {
    val op1 = LinearInsertOperation(0,0,OperationId(),OperationContext(),ClientId())
    val op2 = LinearInsertOperation(0,'a',OperationId(),OperationContext(),ClientId())

    an[IllegalArgumentException] should be thrownBy LinearTransformer.transform((op1, op2))
  }

}
