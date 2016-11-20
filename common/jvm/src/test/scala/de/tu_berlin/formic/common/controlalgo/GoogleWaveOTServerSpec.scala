package de.tu_berlin.formic.common.controlalgo

import de.tu_berlin.formic.common.datatype.{DataTypeOperation, HistoryBuffer, OperationContext, OperationTransformer}
import de.tu_berlin.formic.common.{ClientId, OperationId}
import org.scalatest.{FlatSpec, Matchers}

/**
  * @author Ronny Bräunlich
  */
class GoogleWaveOTServerSpec extends FlatSpec with Matchers {

  "Google Wave OT" should "state that an operation is ready when the history contains exactly the operation of its operation context" in {
    val controlAlgo = new GoogleWaveOTServer()
    val previousOperation = GoogleWaveOTTestOperation(OperationId(), OperationContext(List.empty), ClientId())
    val operation = GoogleWaveOTTestOperation(OperationId(), OperationContext(List(previousOperation.id)), ClientId())
    val history = new HistoryBuffer
    history.addOperation(previousOperation)

    val canBeApplied = controlAlgo.canBeApplied(operation, history)

    canBeApplied should be(true)
  }

  "Google Wave OT" should "state that an operation is ready when the operation has an empty operation context" in {
    val controlAlgo = new GoogleWaveOTServer()
    val operation = GoogleWaveOTTestOperation(OperationId(), OperationContext(List.empty), ClientId(), 0)
    val otherOperation = GoogleWaveOTTestOperation(OperationId(), OperationContext(List.empty), ClientId())
    val history = new HistoryBuffer
    history.addOperation(otherOperation)

    val canBeApplied = controlAlgo.canBeApplied(operation, history)

    canBeApplied should be(true)
  }

  it should "state that an operation is not ready when the history does not contain the operation from its operation context" in {
    val controlAlgo = new GoogleWaveOTServer()
    val operation = GoogleWaveOTTestOperation(OperationId(), OperationContext(List(OperationId())), ClientId())

    val canBeApplied = controlAlgo.canBeApplied(operation, new HistoryBuffer)

    canBeApplied should be(false)
  }

  it should "not transform an operation that has no concurrent counterparts" in {
    val controlAlgo = new GoogleWaveOTServer()
    val operation = GoogleWaveOTTestOperation(OperationId(), OperationContext(List(OperationId())), ClientId())

    val transformed = controlAlgo.transform(operation, new HistoryBuffer, GoogleWaveOTTestTransformer)

    transformed should be(operation)
  }

  it should "transform an operation against all concurrent operations" in {
    val controlAlgo = new GoogleWaveOTServer()
    val operation = GoogleWaveOTTestOperation(OperationId(), OperationContext(List.empty), ClientId())
    val otherOperation = GoogleWaveOTTestOperation(OperationId(), OperationContext(List.empty), ClientId())
    val otherOperation1 = GoogleWaveOTTestOperation(OperationId(), OperationContext(List.empty), ClientId())
    val history = new HistoryBuffer
    history.addOperation(otherOperation)
    history.addOperation(otherOperation1)

    val transformed = controlAlgo.transform(operation, history, GoogleWaveOTTestTransformer)

    transformed should equal(GoogleWaveOTTestOperation(operation.id, OperationContext(List(otherOperation1.id)), operation.clientId, 2))
  }

  it should "not include previous operations in the transformation" in {
    val controlAlgo = new GoogleWaveOTServer()
    val otherOperation = GoogleWaveOTTestOperation(OperationId(), OperationContext(List.empty), ClientId())
    val otherOperation1 = GoogleWaveOTTestOperation(OperationId(), OperationContext(List(otherOperation.id)), ClientId())
    val operation = GoogleWaveOTTestOperation(OperationId(), OperationContext(List(otherOperation.id)), ClientId())
    val history = new HistoryBuffer
    history.addOperation(otherOperation)
    history.addOperation(otherOperation1)

    val transformed = controlAlgo.transform(operation, history, GoogleWaveOTTestTransformer)

    transformed should equal(GoogleWaveOTTestOperation(operation.id, OperationContext(List(otherOperation1.id)), operation.clientId, 1))
  }

  it should "perform the transformations in correct order" in {
    val controlAlgo = new GoogleWaveOTServer()
    val otherOperation = GoogleWaveOTTestOperation(OperationId(), OperationContext(List.empty), ClientId())
    val otherOperation1 = GoogleWaveOTTestOperation(OperationId(), OperationContext(List.empty), ClientId())
    val otherOperation2 = GoogleWaveOTTestOperation(OperationId(), OperationContext(List.empty), ClientId())
    val operation = GoogleWaveOTTestOperation(OperationId(), OperationContext(List.empty), ClientId())
    val history = new HistoryBuffer
    history.addOperation(otherOperation)
    history.addOperation(otherOperation1)
    history.addOperation(otherOperation2)

    val transformed = controlAlgo.transform(operation, history, GoogleWaveOTTestTransformer)

    transformed should equal(GoogleWaveOTTestOperation(operation.id, OperationContext(List(otherOperation2.id)), operation.clientId, 3))
  }
}

case class GoogleWaveOTTestOperation(id: OperationId, operationContext: OperationContext, clientId: ClientId, transformations: Int = 0) extends DataTypeOperation

object GoogleWaveOTTestTransformer extends OperationTransformer {

  override def transform(pair: (DataTypeOperation, DataTypeOperation)): DataTypeOperation = {
    val toTransform = pair._1.asInstanceOf[GoogleWaveOTTestOperation]
    GoogleWaveOTTestOperation(toTransform.id, OperationContext(List(pair._2.id)), toTransform.clientId, toTransform.transformations + 1)
  }
}
