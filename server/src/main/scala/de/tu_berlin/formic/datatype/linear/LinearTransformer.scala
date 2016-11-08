package de.tu_berlin.formic.datatype.linear

import de.tu_berlin.formic.common.{DataTypeOperation, OperationContext, OperationId}

trait LinearStructureOperation extends DataTypeOperation{
  val index:Int
}

case class LinearInsertOperation(index: Int, o: Object, id: OperationId, operationContext: OperationContext) extends LinearStructureOperation

case class LinearDeleteOperation(index: Int, id: OperationId, operationContext: OperationContext) extends LinearStructureOperation

/**
  * @author Ronny Bräunlich
  */
object LinearTransformer {

  def transform(o1: LinearDeleteOperation, o2: LinearDeleteOperation): LinearStructureOperation = {
    if (o1.index < o2.index) return o1
    else if (o1.index > o2.index) return LinearDeleteOperation(o1.index - 1, o1.id, o1.operationContext)
    //the no-op operation
    return new LinearStructureOperation{
      val index = -1
      val id = o1.id
      val operationContext = o1.operationContext
    }
  }

  def transform(o1: LinearDeleteOperation, o2: LinearInsertOperation): LinearDeleteOperation = {
    return null
  }

  def transform(o1: LinearInsertOperation, o2: LinearInsertOperation): LinearInsertOperation = {
    return null
  }

  def transform(o1: LinearInsertOperation, o2: LinearDeleteOperation): LinearInsertOperation = {
    return null
  }
}