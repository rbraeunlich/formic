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
    * @param pair A pair of operations. In order to use pattern matching it is a tuple.
    * @return The first operation that includes the effect of the second one.
    */
  def transform(pair: (DataTypeOperation, DataTypeOperation)): DataTypeOperation

}
