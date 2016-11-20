package de.tu_berlin.formic.common.controlalgo

import de.tu_berlin.formic.common.datatype.{DataTypeOperation, HistoryBuffer, OperationTransformer}

/**
  * The implementation of the Google Wave OT algorithm on the server side
  *
  * @author Ronny Bräunlich
  */
class GoogleWaveOTServer extends ControlAlgorithm {
  /**
    * Decides if an operation is causally ready to be applied. If not, the data type has to
    * store the operation somewhere to be checked for later application.
    *
    * @param op      the operation that shall be applied
    * @param history the history of already applied operations of the data type instance
    * @return true if the operation can be applied
    */
  override def canBeApplied(op: DataTypeOperation, history: HistoryBuffer): Boolean = {
    val directAncestorOperation = op.operationContext.operations.headOption
    directAncestorOperation match {
      case None => true
      case Some(ancestorId) =>
        val foundInHistory = history.findOperation(ancestorId)
        foundInHistory match {
          case None => false
          case Some(_) => true
        }
    }

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
    val directAncestorOperation = op.operationContext.operations.headOption
    val parallelOperations = directAncestorOperation match {
      case None => history.history
      case Some(ancestorId) => history.findAllOperationsAfter(ancestorId)
    }
    //the history goes from the youngest to the oldest, therefore we have to transform from the back
    //because foldRight applies coll(n-1) op init, we switch them for the transform function
    parallelOperations.foldRight(op)((o1, o2) => transformer.transform((o2, o1)))
  }
}
