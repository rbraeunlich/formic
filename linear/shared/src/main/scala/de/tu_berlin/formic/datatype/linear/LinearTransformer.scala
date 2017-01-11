package de.tu_berlin.formic.datatype.linear

import de.tu_berlin.formic.common.datatype.{DataTypeOperation, OperationContext, OperationTransformer}
import de.tu_berlin.formic.common.{ClientId, OperationId}

trait LinearStructureOperation extends DataTypeOperation {
  val index: Int
}

case class LinearInsertOperation(index: Int, o: Any, id: OperationId, operationContext: OperationContext, var clientId: ClientId) extends LinearStructureOperation

case class LinearDeleteOperation(index: Int, id: OperationId, operationContext: OperationContext, var clientId: ClientId) extends LinearStructureOperation

case class LinearNoOperation(id: OperationId, operationContext: OperationContext, var clientId: ClientId) extends LinearStructureOperation {
  override val index: Int = -1
}

/**
  * Transforms two operations according to the IT rules in a TP1 valid way.
  *
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
    transformInternal(pair, withNewContext = true)
  }

  private def transformInternal(pair: (DataTypeOperation, DataTypeOperation), withNewContext: Boolean): DataTypeOperation = {
    val context = if (withNewContext) OperationContext(List(pair._2.id)) else pair._1.operationContext
    pair match {
      case (op1: LinearDeleteOperation, op2: LinearDeleteOperation) => transform(op1, op2, context)
      case (op1: LinearInsertOperation, op2: LinearInsertOperation) => transform(op1, op2, context)
      case (op1: LinearInsertOperation, op2: LinearDeleteOperation) => transform(op1, op2, context)
      case (op1: LinearDeleteOperation, op2: LinearInsertOperation) => transform(op1, op2, context)
      case (op1: LinearInsertOperation, op2: LinearNoOperation) => LinearInsertOperation(op1.index, op1.o, op1.id, context, op1.clientId)
      case (op1: LinearDeleteOperation, op2: LinearNoOperation) => LinearDeleteOperation(op1.index, op1.id, context, op1.clientId)
      case (op1: LinearNoOperation, op2: LinearStructureOperation) => LinearNoOperation(op1.id, context, op1.clientId)
    }
  }

  private def transform(o1: LinearDeleteOperation, o2: LinearDeleteOperation, newContext: OperationContext): LinearStructureOperation = {
    if (o1.index < o2.index) return LinearDeleteOperation(o1.index, o1.id, newContext, o1.clientId)
    else if (o1.index > o2.index) return LinearDeleteOperation(o1.index - 1, o1.id, newContext, o1.clientId)
    LinearNoOperation(o1.id, newContext, o1.clientId)
  }

  private def transform(o1: LinearDeleteOperation, o2: LinearInsertOperation, newContext: OperationContext): LinearStructureOperation = {
    if (o1.index < o2.index) LinearDeleteOperation(o1.index, o1.id, newContext, o1.clientId)
    else LinearDeleteOperation(o1.index + 1, o1.id, newContext, o1.clientId)
  }

  private def transform(o1: LinearInsertOperation, o2: LinearInsertOperation, newContext: OperationContext): LinearStructureOperation = {
    if (o1.index < o2.index) LinearInsertOperation(o1.index, o1.o, o1.id, newContext, o1.clientId)
    else if (o1.index > o2.index) LinearInsertOperation(o1.index + 1, o1.o, o1.id, newContext, o1.clientId)
    else if (o1.o == o2.o) LinearNoOperation(o1.id, newContext, o1.clientId)
    else if (o1.clientId > o2.clientId) LinearInsertOperation(o1.index, o1.o, o1.id, newContext, o1.clientId)
    else LinearInsertOperation(o1.index + 1, o1.o, o1.id, newContext, o1.clientId)
  }

  private def transform(o1: LinearInsertOperation, o2: LinearDeleteOperation, newContext: OperationContext): LinearStructureOperation = {
    if (o1.index <= o2.index) LinearInsertOperation(o1.index, o1.o, o1.id, newContext, o1.clientId)
    else LinearInsertOperation(o1.index - 1, o1.o, o1.id, newContext, o1.clientId)
  }

  /**
    * Performs a bulk transformation against all operations present in the bridge.
    * Only the first operation in the bridge receives a new context, the others retain
    * their original one.
    *
    * @param operation the operation that is transformed against the bridge
    * @param bridge    the operations that have to be transformed
    */
  override def bulkTransform(operation: DataTypeOperation, bridge: List[DataTypeOperation]): List[DataTypeOperation] = {
    if (bridge.isEmpty) return bridge
    val operationToChangeContext = bridge.last
    val others = bridge.take(bridge.size - 1)
    val transformedOperation = transform((operationToChangeContext, operation))
    val transformedOthers = others.map(op => transformInternal((op, operation), withNewContext = false))
    transformedOthers :+ transformedOperation
  }
}