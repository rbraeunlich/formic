package de.tu_berlin.formic.common.controlalgo

import de.tu_berlin.formic.common.OperationId
import de.tu_berlin.formic.common.datastructure.{DataStructureOperation, HistoryBuffer, OperationContext, OperationTransformer}

/**
  * The client implementation of the Wave OT algorithm. In order to keep it free from
  * Actors etc., it takes a function as parameter which it uses to send local operations to the
  * server.
  *
  * @author Ronny Bräunlich
  */
class WaveOTClient(sendToServerFunction: (DataStructureOperation) => Unit) extends ControlAlgorithmClient {

  var buffer: List[DataStructureOperation] = List.empty

  var inFlightOperation: DataStructureOperation = _

  var currentContext: List[OperationId] = List.empty

  /**
    * Decides if an operation is causally ready to be applied. If not, the data type has to
    * store the operation somewhere to be checked for later application.
    *
    * @param op      the operation that shall be applied
    * @param history the history of already applied operations of the data type instance
    * @return true if the operation can be applied
    */
  override def canBeApplied(op: DataStructureOperation, history: HistoryBuffer): Boolean = {
    if (isAcknowledgement(op)) {
      if (buffer.nonEmpty) {
        inFlightOperation = buffer.head
        buffer = buffer.drop(1)
        sendToServerFunction(inFlightOperation)
      } else {
        inFlightOperation = null
      }
      //local operations have already been applied, therefore we answer false here
      return false
    }
    true
  }

  /**
    * Performs operational transformation on an operation if necessary
    *
    * @param op          the operation that might be transformed
    * @param history     the history of already applied operations of the data type instance
    * @param transformer the transformer that knows the transformation rules
    * @return an operation that can be applied to the data type instance
    */
  override def transform(op: DataStructureOperation, history: HistoryBuffer, transformer: OperationTransformer): DataStructureOperation = {
    var transformed = op
    if (inFlightOperation != null) {
      transformed = (inFlightOperation +: buffer).foldLeft(transformed)((o1, o2) => transformer.transform((o1, o2)))
      //now we have to transform the whole bridge
      val transformedBridge = transformer.bulkTransform(op, buffer.reverse :+ inFlightOperation)
      inFlightOperation = transformedBridge.last
      buffer = transformedBridge.take(transformedBridge.size - 1).reverse
    } else {
      currentContext = List(transformed.id)
    }
    transformed
  }

  /**
    * Decides if an operation that has be generated on the client is causally ready to be applied.
    *
    * @param op      the operation that shall be applied
    * @return true if the operation can be applieds
    */
  override def canLocalOperationBeApplied(op: DataStructureOperation): Boolean = {
    if (inFlightOperation == null) {
      inFlightOperation = op
      sendToServerFunction(inFlightOperation)
    } else {
      buffer = buffer :+ op
    }
    currentContext = List(op.id)
    true
  }

  def isAcknowledgement(op: DataStructureOperation): Boolean = {
    //acknowledgements can never come out of order
    inFlightOperation != null && op.id == inFlightOperation.id
  }

  def currentOperationContext = OperationContext(currentContext)
}
