package de.tu_berlin.formic.datatype.linear

import de.tu_berlin.formic.common.datatype.{DataTypeOperation, OperationContext, OperationTransformer}
import de.tu_berlin.formic.common.{ClientId, OperationId}

trait LinearStructureOperation extends DataTypeOperation {
  val index: Int
}

case class LinearInsertOperation(index: Int, o: Any, id: OperationId, operationContext: OperationContext,var clientId: ClientId) extends LinearStructureOperation

case class LinearDeleteOperation(index: Int, id: OperationId, operationContext: OperationContext,var clientId: ClientId) extends LinearStructureOperation

case class LinearNoOperation(index: Int, id: OperationId, operationContext: OperationContext,var clientId: ClientId) extends LinearStructureOperation

/**
  * Transforms two operations according to the IT rules in a TP1 valid way.
  * @author Ronny Bräunlich
  */
object LinearTransformer extends OperationTransformer {

  /**
    * Inclusion transforms a pair of operations. After transformation the second operation is included in the
    * operation context of the first operation. IT(O1,O2)=O'1
    *
    * @param pair A pair of operations. In order to use pattern matching it is a tuple.
    * @return The first operation that includes the effect of the second one.
    */
  override def transform(pair: (DataTypeOperation, DataTypeOperation)): DataTypeOperation = {
    pair match {
      case (op1: LinearDeleteOperation, op2: LinearDeleteOperation) => transform(op1, op2)
      case (op1: LinearInsertOperation, op2: LinearInsertOperation) => transform(op1, op2)
      case (op1: LinearInsertOperation, op2: LinearDeleteOperation) => transform(op1, op2)
      case (op1: LinearDeleteOperation, op2: LinearInsertOperation) => transform(op1, op2)
    }
  }

  private def transform(o1: LinearDeleteOperation, o2: LinearDeleteOperation): LinearStructureOperation = {
    if (o1.index < o2.index) return LinearDeleteOperation(o1.index, o1.id, OperationContext(List(o2.id)), o1.clientId)
    else if (o1.index > o2.index) return LinearDeleteOperation(o1.index - 1, o1.id, OperationContext(List(o2.id)), o1.clientId)
    LinearNoOperation(-1, o1.id, OperationContext(List(o2.id)), o1.clientId)
  }

  private def transform(o1: LinearDeleteOperation, o2: LinearInsertOperation): LinearStructureOperation = {
    if (o1.index <= o2.index) LinearDeleteOperation(o1.index, o1.id, OperationContext(List(o2.id)), o1.clientId)
    else LinearDeleteOperation(o1.index + 1, o1.id, OperationContext(List(o2.id)), o1.clientId)
  }

  private def transform(o1: LinearInsertOperation, o2: LinearInsertOperation): LinearStructureOperation = {
    if (o1.index < o2.index) LinearInsertOperation(o1.index, o1.o, o1.id, OperationContext(List(o2.id)), o1.clientId)
    else if (o1.index > o2.index) LinearInsertOperation(o1.index + 1, o1.o, o1.id, OperationContext(List(o2.id)), o1.clientId)
    else if (o1.o == o2.o) LinearNoOperation(-1, o1.id, OperationContext(List(o2.id)), o1.clientId)
    else if (o1.clientId > o2.clientId) LinearInsertOperation(o1.index, o1.o, o1.id, OperationContext(List(o2.id)), o1.clientId)
    else LinearInsertOperation(o1.index + 1, o1.o, o1.id, OperationContext(List(o2.id)), o1.clientId)
  }

  private def transform(o1: LinearInsertOperation, o2: LinearDeleteOperation): LinearStructureOperation = {
    if (o1.index < o2.index) LinearInsertOperation(o1.index, o1.o, o1.id, OperationContext(List(o2.id)), o1.clientId)
    else LinearInsertOperation(o1.index - 1, o1.o, o1.id, OperationContext(List(o2.id)), o1.clientId)
  }

}