package de.tu_berlin.formic.common.controlalgo

import de.tu_berlin.formic.common.datastructure.{DataStructureOperation, HistoryBuffer, OperationTransformer}

/**
  * Common trait for all control algorithms
  *
  * @author Ronny Bräunlich
  */
trait ControlAlgorithm {

  /**
    * Decides if an operation is causally ready to be applied. If not, the data type has to
    * store the operation somewhere to be checked for later application.
    * @param op the operation that shall be applied
    * @param history the history of already applied operations of the data type instance
    * @return true if the operation can be applied
    */
  def canBeApplied(op: DataStructureOperation, history: HistoryBuffer): Boolean

  /**
    * Performs operational transformation on an operation if necessary
    * @param op the operation that might be transformed
    * @param history the history of already applied operations of the data type instance
    * @param transformer the transformer that knows the transformation rules
    * @return an operation that can be applied to the data type instance
    */
  def transform(op: DataStructureOperation, history: HistoryBuffer, transformer: OperationTransformer): DataStructureOperation

}
