package de.tu_berlin.formic.common.datatype

/**
  * Common interface for classes that apply operational transformation to two operations.
  * Each data type needs its own implementation of this interface in order to work with the control algorithm.
  *
  * @author Ronny Bräunlich
  */
trait OperationTransformer {

  /**
    * Inclusion transforms a pair of operations. After transformation the second operation is included in the
    * operation context of the first operation. IT(O1,O2)=O'1
    *
    * @param pair A pair of operations. In order to use pattern matching it is a tuple.
    * @return The first operation that includes the effect of the second one.
    */
  def transform(pair: (DataTypeOperation, DataTypeOperation)): DataTypeOperation

  /**
    * Performs a bulk transformation against all operations present in the bridge.
    * Only the first operation in the bridge receives a new context, the others retain
    * their original one.
    *
    * @param operation the operation that is transformed against the bridge
    * @param bridge    the operations that have to be transformed
    */
  def bulkTransform(operation: DataTypeOperation, bridge: List[DataTypeOperation]): List[DataTypeOperation] = {
    if (bridge.isEmpty) return bridge
    val operationToChangeContext = bridge.last
    val others = bridge.take(bridge.size - 1)
    val transformedOperation = transform((operationToChangeContext, operation))
    val transformedIncomingOperation = transform((operation, operationToChangeContext))

    val transformedOthers = others.reverse.foldLeft((transformedIncomingOperation, List.empty[DataTypeOperation]))((t, op) => {
      val opPrime = transformInternal((op, t._1), withNewContext = false)
      val incomingPrime = transformInternal((t._1, op), withNewContext = false)
      (incomingPrime, opPrime +: t._2)
    })
    transformedOthers._2 :+ transformedOperation
  }


  protected def transformInternal(pair: (DataTypeOperation, DataTypeOperation), withNewContext: Boolean): DataTypeOperation

}
