package de.tu_berlin.formic.common.controlalgo

import de.tu_berlin.formic.common.datatype.{DataTypeOperation, HistoryBuffer, OperationTransformer}

/**
  * The client implementation of the Google Wave OT algorithm. In order to keep it free from
  * Actors etc., it takes a function as parameter which it uses to send loca operations to the
  * server.
  *
  * @author Ronny Bräunlich
  */
class GoogleWaveOTClient(sendToServerFunction: (DataTypeOperation) => Unit) extends ControlAlgorithmClient {

  var buffer: List[DataTypeOperation] = List.empty

  var inFlightOperation: DataTypeOperation = _

  /**
    * Decides if an operation is causally ready to be applied. If not, the data type has to
    * store the operation somewhere to be checked for later application.
    *
    * @param op      the operation that shall be applied
    * @param history the history of already applied operations of the data type instance
    * @return true if the operation can be applied
    */
  override def canBeApplied(op: DataTypeOperation, history: HistoryBuffer): Boolean = {
    if (isAcknowledgement(op)) {
      if (buffer.nonEmpty) {
        inFlightOperation = buffer.head
        buffer = buffer.drop(0)
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
  override def transform(op: DataTypeOperation, history: HistoryBuffer, transformer: OperationTransformer): DataTypeOperation = {
    var transformed = op
    if (inFlightOperation != null) {
      transformed = (inFlightOperation +: buffer).foldLeft(transformed)((o1, o2) => transformer.transform((o1, o2)))
    }
    transformed
  }

  /**
    * Decides if an operation that has be generated on the client is causally ready to be applied.
    *
    * @param op      the operation that shall be applied
    * @param history the history of already applied operations of the data type instance
    * @return true if the operation can be applied
    */
  override def canLocalOperationBeApplied(op: DataTypeOperation, history: HistoryBuffer): Boolean = {
    if (inFlightOperation == null) {
      inFlightOperation = op
      sendToServerFunction(inFlightOperation)
    } else {
      buffer = buffer :+ op
    }
    true
  }

  def isAcknowledgement(op: DataTypeOperation): Boolean = {
    //acknowledgements can never come out of order
    inFlightOperation != null && op.id == inFlightOperation.id
  }
}
