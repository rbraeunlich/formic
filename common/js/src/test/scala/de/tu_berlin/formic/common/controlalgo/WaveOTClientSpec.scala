package de.tu_berlin.formic.common.controlalgo

import de.tu_berlin.formic.common.datatype.{DataTypeOperation, HistoryBuffer, OperationContext, OperationTransformer}
import de.tu_berlin.formic.common.{ClientId, OperationId}
import org.scalatest.{Matchers, WordSpec}

/**
  * @author Ronny Bräunlich
  */
class WaveOTClientSpec extends WordSpec with Matchers {

  "WaveOTClient" must {
    "make first local operation in-flight operation and send it to the server" in {
      val operation = WaveOTClientTestOperation(OperationId(), OperationContext(List.empty), ClientId())
      val controlAlgo = new WaveOTClient((op) => op should equal(operation))
      val canBeApplied = controlAlgo.canLocalOperationBeApplied(operation)

      controlAlgo.inFlightOperation should equal(operation)
      canBeApplied should equal(true)
    }

    "add operations to the buffer if an in-flight operation is present" in {
      val controlAlgo = new WaveOTClient((op) => ())
      val operation = WaveOTClientTestOperation(OperationId(), OperationContext(List.empty), ClientId())
      val operation2 = WaveOTClientTestOperation(OperationId(), OperationContext(List.empty), ClientId())
      val operation3 = WaveOTClientTestOperation(OperationId(), OperationContext(List.empty), ClientId())
      controlAlgo.canLocalOperationBeApplied(operation)
      controlAlgo.canLocalOperationBeApplied(operation2)
      val canBeApplied = controlAlgo.canLocalOperationBeApplied(operation3)

      controlAlgo.buffer should contain inOrder(operation2, operation3)
      canBeApplied should equal(true)
    }

    "remove the in-flight operation if remote operation is an acknowledgement and return false" in {
      val controlAlgo = new WaveOTClient((op) => ())
      val operation = WaveOTClientTestOperation(OperationId(), OperationContext(List.empty), ClientId())
      controlAlgo.canLocalOperationBeApplied(operation)
      val canBeApplied = controlAlgo.canBeApplied(operation, new HistoryBuffer())

      controlAlgo.inFlightOperation should be(null)
      canBeApplied should equal(false)
    }

    "make next operation from the buffer the new in-flight operation if remote operation is an acknowledgement, return false and send new in-flight to the server" in {
      val operation = WaveOTClientTestOperation(OperationId(), OperationContext(List.empty), ClientId())
      val operation2 = WaveOTClientTestOperation(OperationId(), OperationContext(List(operation.id)), ClientId())
      val operation3 = WaveOTClientTestOperation(OperationId(), OperationContext(List(operation2.id)), ClientId())
      var first = true
      val controlAlgo = new WaveOTClient((op) =>
        if (first) op should equal(operation)
        else op should equal(operation2)
      )
      controlAlgo.canLocalOperationBeApplied(operation)
      first = false
      controlAlgo.canLocalOperationBeApplied(operation2)
      controlAlgo.canLocalOperationBeApplied(operation3)
      val canBeApplied = controlAlgo.canBeApplied(operation, new HistoryBuffer())

      controlAlgo.inFlightOperation should be(operation2)
      controlAlgo.buffer should contain only operation3
      canBeApplied should equal(false)
    }

    "state that remote operations can be applied" in {
      val controlAlgo = new WaveOTClient((op) => ())
      val operation = WaveOTClientTestOperation(OperationId(), OperationContext(List.empty), ClientId())
      val canBeApplied = controlAlgo.canBeApplied(operation, new HistoryBuffer())

      canBeApplied should equal(true)
    }

    "not transform remote operations if no inFlightOperation is present" in {
      val controlAlgo = new WaveOTClient((op) => ())
      val operation = WaveOTClientTestOperation(OperationId(), OperationContext(List.empty), ClientId())
      val transformed = controlAlgo.transform(operation, new HistoryBuffer(), WaveOTClientTestTransformer)

      transformed should equal(operation)
    }

    "transform remote operations against inFlightOperation if present" in {
      val operationId = OperationId()
      val clientId = ClientId()
      val controlAlgo = new WaveOTClient((op) => ())
      val operation = WaveOTClientTestOperation(OperationId(), OperationContext(List.empty), ClientId())
      val operation2 = WaveOTClientTestOperation(operationId, OperationContext(List(operation.id)), clientId)
      controlAlgo.canLocalOperationBeApplied(operation)
      val transformed = controlAlgo.transform(operation2, new HistoryBuffer(), WaveOTClientTestTransformer)

      transformed should equal(WaveOTClientTestOperation(operationId, OperationContext(List(operation.id)), clientId, 1))
    }

    "transform remote operations against inFlightOperation and whole buffer if not empty" in {
      val operationId = OperationId()
      val clientId = ClientId()
      val controlAlgo = new WaveOTClient((op) => ())
      val operation = WaveOTClientTestOperation(OperationId(), OperationContext(List.empty), ClientId())
      val operation2 = WaveOTClientTestOperation(OperationId(), OperationContext(List(operation.id)), ClientId())
      val operation3 = WaveOTClientTestOperation(OperationId(), OperationContext(List(operation2.id)), ClientId())
      val operation4 = WaveOTClientTestOperation(operationId, OperationContext(List.empty), clientId)
      controlAlgo.canLocalOperationBeApplied(operation)
      controlAlgo.canLocalOperationBeApplied(operation2)
      controlAlgo.canLocalOperationBeApplied(operation3)
      val transformed = controlAlgo.transform(operation4, new HistoryBuffer(), WaveOTClientTestTransformer)

      transformed should equal(WaveOTClientTestOperation(operationId, OperationContext(List(operation3.id)), clientId, 3))
    }

    "replace the whole translation bridge (in-flight + buffer) with their transformation with the remote operation" in {
      val operationId = OperationId()
      val clientId = ClientId()
      val controlAlgo = new WaveOTClient((op) => ())
      val operation = WaveOTClientTestOperation(OperationId(), OperationContext(List.empty), ClientId())
      val operation2 = WaveOTClientTestOperation(OperationId(), OperationContext(List(operation.id)), ClientId())
      val operation3 = WaveOTClientTestOperation(OperationId(), OperationContext(List(operation2.id)), ClientId())
      val operation4 = WaveOTClientTestOperation(operationId, OperationContext(List.empty), clientId)
      controlAlgo.canLocalOperationBeApplied(operation)
      controlAlgo.canLocalOperationBeApplied(operation2)
      controlAlgo.canLocalOperationBeApplied(operation3)

      controlAlgo.transform(operation4, new HistoryBuffer(), WaveOTClientTestTransformer)
      controlAlgo.inFlightOperation should equal(WaveOTClientTestOperation(operation.id, OperationContext(List(operation4.id)), operation.clientId, 1))
      controlAlgo.buffer should contain inOrder(
        WaveOTClientTestOperation(operation2.id, OperationContext(List(operation4.id)), operation2.clientId, 1),
        WaveOTClientTestOperation(operation3.id, OperationContext(List(operation4.id)), operation3.clientId, 1)
        )
    }
  }
}


case class WaveOTClientTestOperation(id: OperationId, operationContext: OperationContext, var clientId: ClientId, transformations: Int = 0) extends DataTypeOperation

object WaveOTClientTestTransformer extends OperationTransformer {

  override def transform(pair: (DataTypeOperation, DataTypeOperation)): DataTypeOperation = {
    val toTransform = pair._1.asInstanceOf[WaveOTClientTestOperation]
    WaveOTClientTestOperation(toTransform.id, OperationContext(List(pair._2.id)), toTransform.clientId, toTransform.transformations + 1)
  }
}