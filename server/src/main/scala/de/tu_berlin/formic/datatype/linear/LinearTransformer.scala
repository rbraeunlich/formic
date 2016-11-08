package de.tu_berlin.formic.datatype.linear

import de.tu_berlin.formic.common.OperationId

trait LinearStructureOperation

case class LinearInsertOperation(index: Int, o: Object, id: OperationId) extends LinearStructureOperation

case class LinearDeleteOperation(index: Int, id: OperationId)

/**
  * @author Ronny Bräunlich
  */
object LinearTransformer {

  def transform(o1: LinearDeleteOperation, o2: LinearDeleteOperation): LinearDeleteOperation = {
    return null
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