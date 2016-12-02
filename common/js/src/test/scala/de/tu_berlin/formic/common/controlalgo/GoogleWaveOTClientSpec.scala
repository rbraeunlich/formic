package de.tu_berlin.formic.common.controlalgo

import de.tu_berlin.formic.common.datatype.{DataTypeOperation, HistoryBuffer, OperationContext, OperationTransformer}
import de.tu_berlin.formic.common.{ClientId, OperationId}
import org.scalatest.{Matchers, WordSpec}

/**
  * @author Ronny Bräunlich
  */
class GoogleWaveOTClientSpec extends WordSpec with Matchers {

  "GoogleWaveOTClient" must {
    "make first local operation in-flight operation and send it to the server" in {
      val operation = GoogleWaveOTClientTestOperation(OperationId(), OperationContext(List.empty), ClientId())
      val controlAlgo = new GoogleWaveOTClient((op) => op should equal(operation))
      val canBeApplied = controlAlgo.canLocalOperationBeApplied(operation, new HistoryBuffer())

      controlAlgo.inFlightOperation should equal(operation)
      canBeApplied should equal(true)
    }

    "add operations to the buffer if an in-flight operation is present" in {
      val controlAlgo = new GoogleWaveOTClient((op) => ())
      val operation = GoogleWaveOTClientTestOperation(OperationId(), OperationContext(List.empty), ClientId())
      val operation2 = GoogleWaveOTClientTestOperation(OperationId(), OperationContext(List.empty), ClientId())
      val operation3 = GoogleWaveOTClientTestOperation(OperationId(), OperationContext(List.empty), ClientId())
      controlAlgo.canLocalOperationBeApplied(operation, new HistoryBuffer())
      controlAlgo.canLocalOperationBeApplied(operation2, new HistoryBuffer())
      val canBeApplied = controlAlgo.canLocalOperationBeApplied(operation3, new HistoryBuffer())

      controlAlgo.buffer should contain inOrder(operation2, operation3)
      canBeApplied should equal(true)
    }

    "remove the in-flight operation if remote operation is an acknowledgement and return false" in {
      val controlAlgo = new GoogleWaveOTClient((op) => ())
      val operation = GoogleWaveOTClientTestOperation(OperationId(), OperationContext(List.empty), ClientId())
      controlAlgo.canLocalOperationBeApplied(operation, new HistoryBuffer())
      val canBeApplied = controlAlgo.canBeApplied(operation, new HistoryBuffer())

      controlAlgo.inFlightOperation should be(null)
      canBeApplied should equal(false)
    }

    "make next operation from the buffer the new in-flight operation if remote operation is an acknowledgement, return false and send new in-flight to the server" in {
      val operation = GoogleWaveOTClientTestOperation(OperationId(), OperationContext(List.empty), ClientId())
      val operation2 = GoogleWaveOTClientTestOperation(OperationId(), OperationContext(List.empty), ClientId())
      val operation3 = GoogleWaveOTClientTestOperation(OperationId(), OperationContext(List.empty), ClientId())
      var first = true
      val controlAlgo = new GoogleWaveOTClient((op) =>
        if (first) op should equal(operation)
        else op should equal(operation2)
      )
      controlAlgo.canLocalOperationBeApplied(operation, new HistoryBuffer())
      first = false
      controlAlgo.canLocalOperationBeApplied(operation2, new HistoryBuffer())
      controlAlgo.canLocalOperationBeApplied(operation3, new HistoryBuffer())
      val canBeApplied = controlAlgo.canBeApplied(operation, new HistoryBuffer())

      controlAlgo.inFlightOperation should be(operation2)
      controlAlgo.buffer should contain(operation3)
      canBeApplied should equal(false)
    }

    "state that remote operations can be applied" in {
      val controlAlgo = new GoogleWaveOTClient((op) => ())
      val operation = GoogleWaveOTClientTestOperation(OperationId(), OperationContext(List.empty), ClientId())
      val canBeApplied = controlAlgo.canBeApplied(operation, new HistoryBuffer())

      canBeApplied should equal(true)
    }

    "not transform remote operations if no inFlightOperation is present" in {
      val controlAlgo = new GoogleWaveOTClient((op) => ())
      val operation = GoogleWaveOTClientTestOperation(OperationId(), OperationContext(List.empty), ClientId())
      val transformed = controlAlgo.transform(operation, new HistoryBuffer(), GoogleWaveOTClientTestTransformer)

      transformed should equal(operation)
    }

    "transform remote operations against inFlightOperation if present" in {
      val operationId = OperationId()
      val clientId = ClientId()
      val controlAlgo = new GoogleWaveOTClient((op) => ())
      val operation = GoogleWaveOTClientTestOperation(OperationId(), OperationContext(List.empty), ClientId())
      val operation2 = GoogleWaveOTClientTestOperation(operationId, OperationContext(List.empty), clientId)
      controlAlgo.canLocalOperationBeApplied(operation, new HistoryBuffer())
      val transformed = controlAlgo.transform(operation2, new HistoryBuffer(), GoogleWaveOTClientTestTransformer)

      transformed should equal(GoogleWaveOTClientTestOperation(operationId, OperationContext(List(operation.id)), clientId, 1))
    }

    "transform remote operations against inFlightOperation and whole buffer if not empty" in {
      val operationId = OperationId()
      val clientId = ClientId()
      val controlAlgo = new GoogleWaveOTClient((op) => ())
      val operation = GoogleWaveOTClientTestOperation(OperationId(), OperationContext(List.empty), ClientId())
      val operation2 = GoogleWaveOTClientTestOperation(OperationId(), OperationContext(List.empty), ClientId())
      val operation3 = GoogleWaveOTClientTestOperation(OperationId(), OperationContext(List.empty), ClientId())
      val operation4 = GoogleWaveOTClientTestOperation(operationId, OperationContext(List.empty), clientId)
      controlAlgo.canLocalOperationBeApplied(operation, new HistoryBuffer())
      controlAlgo.canLocalOperationBeApplied(operation2, new HistoryBuffer())
      controlAlgo.canLocalOperationBeApplied(operation3, new HistoryBuffer())
      val transformed = controlAlgo.transform(operation4, new HistoryBuffer(), GoogleWaveOTClientTestTransformer)

      transformed should equal(GoogleWaveOTClientTestOperation(operationId, OperationContext(List(operation3.id)), clientId, 3))
    }
  }
}


case class GoogleWaveOTClientTestOperation(id: OperationId, operationContext: OperationContext, var clientId: ClientId, transformations: Int = 0) extends DataTypeOperation

object GoogleWaveOTClientTestTransformer extends OperationTransformer {

  override def transform(pair: (DataTypeOperation, DataTypeOperation)): DataTypeOperation = {
    val toTransform = pair._1.asInstanceOf[GoogleWaveOTClientTestOperation]
    GoogleWaveOTClientTestOperation(toTransform.id, OperationContext(List(pair._2.id)), toTransform.clientId, toTransform.transformations + 1)
  }
}