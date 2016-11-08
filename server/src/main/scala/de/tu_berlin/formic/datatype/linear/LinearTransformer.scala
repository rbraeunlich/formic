package de.tu_berlin.formic.datatype.linear

import de.tu_berlin.formic.common.{ClientId, DataTypeOperation, OperationContext, OperationId}

trait LinearStructureOperation extends DataTypeOperation {
  val index: Int
}

case class LinearInsertOperation(index: Int, o: Object, id: OperationId, operationContext: OperationContext, clientId: ClientId) extends LinearStructureOperation

case class LinearDeleteOperation(index: Int, id: OperationId, operationContext: OperationContext, clientId: ClientId) extends LinearStructureOperation

/**
  * @author Ronny Bräunlich
  */
object LinearTransformer {

  def transform(o1: LinearDeleteOperation, o2: LinearDeleteOperation): LinearStructureOperation = {
    if (o1.index < o2.index) return o1
    else if (o1.index > o2.index) return LinearDeleteOperation(o1.index - 1, o1.id, o1.operationContext, o1.clientId)
    //the no-op operation
    return new LinearStructureOperation {
      val index = -1
      val id = o1.id
      val operationContext = o1.operationContext
      val clientId = o1.clientId
    }
  }

  def transform(o1: LinearDeleteOperation, o2: LinearInsertOperation): LinearStructureOperation = {
    return null
  }

  def transform(o1: LinearInsertOperation, o2: LinearInsertOperation): LinearStructureOperation = {
    if (o1.index < o2.index) return o1
    else if (o1.index > o2.index) return LinearInsertOperation(o1.index + 1, o1.o, o1.id, o1.operationContext, o1.clientId)
    else if (o1.o equals o2.o) return new LinearStructureOperation{
      val index = -1
      val id = o1.id
      val operationContext = o1.operationContext
      val clientId = o1.clientId
    }
    else if(o1.clientId > o2.clientId) return o1
    else return LinearInsertOperation(o1.index + 1, o1.o, o1.id, o1.operationContext, o1.clientId)
  }

  def transform(o1: LinearInsertOperation, o2: LinearDeleteOperation): LinearStructureOperation = {
    return null
  }
}