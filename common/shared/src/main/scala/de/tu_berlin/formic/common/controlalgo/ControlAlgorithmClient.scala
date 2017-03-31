package de.tu_berlin.formic.common.controlalgo

import de.tu_berlin.formic.common.datastructure.{DataStructureOperation, OperationContext}

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
    * @return true if the operation can be applied
    */
  def canLocalOperationBeApplied(op: DataStructureOperation): Boolean

  /**
    * On the client it is not necessarily the history that defines the current operation context.
    * If the control algorithm performs a certain reordering between operations, only it can now
    * what the current context should be.
    */
  def currentOperationContext: OperationContext
}
