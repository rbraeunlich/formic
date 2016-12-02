package de.tu_berlin.formic.common.controlalgo

import de.tu_berlin.formic.common.datatype.{DataTypeOperation, HistoryBuffer}

/**
  * Client and server control algorithms may be different. Therefore we have this interface.
  *
  * @author Ronny Bräunlich
  */
trait ControlAlgorithmClient extends ControlAlgorithm {

  /**
    * Decides if an operation that has be generated on the client is causally ready to be applied.
    *
    * @param op      the operation that shall be applied
    * @param history the history of already applied operations of the data type instance
    * @return true if the operation can be applied
    */
  def canLocalOperationBeApplied(op: DataTypeOperation, history: HistoryBuffer): Boolean
}
