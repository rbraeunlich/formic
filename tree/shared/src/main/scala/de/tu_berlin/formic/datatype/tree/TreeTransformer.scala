package de.tu_berlin.formic.datatype.tree

import de.tu_berlin.formic.common.datatype.{DataTypeOperation, OperationContext, OperationTransformer}
import de.tu_berlin.formic.common.{ClientId, OperationId}

trait TreeStructureOperation extends DataTypeOperation {
  val accessPath: AccessPath
}

case class TreeInsertOperation(accessPath: AccessPath, tree: ValueTreeNode, id: OperationId, operationContext: OperationContext, var clientId: ClientId) extends TreeStructureOperation

case class TreeDeleteOperation(accessPath: AccessPath, id: OperationId, operationContext: OperationContext, var clientId: ClientId) extends TreeStructureOperation

case class TreeNoOperation(id: OperationId, operationContext: OperationContext, var clientId: ClientId) extends TreeStructureOperation {
  override val accessPath: AccessPath = AccessPath(-1)
}

/**
  * @author Ronny Bräunlich
  */
object TreeTransformer extends OperationTransformer {

  def transformationPoint(path1: AccessPath, path2: AccessPath): Int = {
    val combined = path1.list.zip(path2.list).zipWithIndex
    val firstDifference = combined.find(threepel => threepel._1._1 != threepel._1._2)
    firstDifference.map(threepel => threepel._2).getOrElse(combined.size - 1)
  }

  def isEffectIndependent(path1: AccessPath, path2: AccessPath): Boolean = {
    val tp = transformationPoint(path1, path2)
    if (path1.list.size > tp + 1 && path2.list.size > tp + 1) true
    else if (path1.list(tp) > path2.list(tp) && path1.list.size < path2.list.size) true
    else if (path1.list(tp) < path2.list(tp) && path1.list.size > path2.list.size) true
    else false
  }

  override def transform(pair: (DataTypeOperation, DataTypeOperation)): DataTypeOperation = {
    transformInternal(pair, withNewContext = true)
  }

  override def bulkTransform(operation: DataTypeOperation, bridge: List[DataTypeOperation]): List[DataTypeOperation] = {
    if (bridge.isEmpty) return bridge
    val operationToChangeContext = bridge.last
    val others = bridge.take(bridge.size - 1)
    val transformedOperation = transform((operationToChangeContext, operation))
    val transformedOthers = others.map(op => transformInternal((op, operation), withNewContext = false))
    transformedOthers :+ transformedOperation
  }

  private def transformInternal(pair: (DataTypeOperation, DataTypeOperation), withNewContext: Boolean): DataTypeOperation = {
    val context = if (withNewContext) OperationContext(List(pair._2.id)) else pair._1.operationContext
    pair match {
      case (op1: TreeInsertOperation, op2: TreeInsertOperation) => transform(op1, op2, context)
      case (op1: TreeDeleteOperation, op2: TreeDeleteOperation) => transform(op1, op2, context)
      case (op1: TreeInsertOperation, op2: TreeDeleteOperation) => transform(op1, op2, context)
      case (op1: TreeDeleteOperation, op2: TreeInsertOperation) => transform(op1, op2, context)
    }
  }


  private def updatePlus(accessPath: AccessPath, tp: Int): AccessPath = {
    AccessPath(accessPath.list.zipWithIndex.map(t => if (t._2 == tp) t._1 + 1 else t._1))
  }

  def updateMinus(accessPath: AccessPath, tp: Int): AccessPath = {
    AccessPath(accessPath.list.zipWithIndex.map(t => if (t._2 == tp) t._1 - 1 else t._1))
  }

  private def transform(op1: TreeInsertOperation, op2: TreeInsertOperation, context: OperationContext): DataTypeOperation = {
    if (isEffectIndependent(op1.accessPath, op2.accessPath)) {
      return TreeInsertOperation(op1.accessPath, op1.tree, op1.id, context, op1.clientId)
    }
    val tp = transformationPoint(op1.accessPath, op2.accessPath)
    if (op1.accessPath.list(tp) < op2.accessPath.list(tp)) {
      return TreeInsertOperation(op1.accessPath, op1.tree, op1.id, context, op1.clientId)
    }
    if (op1.accessPath.list(tp) > op2.accessPath.list(tp)) {
      val newPath = updatePlus(op1.accessPath, tp)
      return TreeInsertOperation(newPath, op1.tree, op1.id, context, op1.clientId)
    }
    //access path at index tp must be equal
    if (op1.accessPath.list.size > op2.accessPath.list.size) {
      val newPath = updatePlus(op1.accessPath, tp)
      return TreeInsertOperation(newPath, op1.tree, op1.id, context, op1.clientId)
    }
    if (op1.accessPath.list.size < op2.accessPath.list.size) {
      return TreeInsertOperation(op1.accessPath, op1.tree, op1.id, context, op1.clientId)
    }
    if (op1.tree == op2.tree) {
      return TreeNoOperation(op1.id, context, op1.clientId)
    }
    if (op1.clientId > op2.clientId) {
      return TreeInsertOperation(op1.accessPath, op1.tree, op1.id, context, op1.clientId)
    }
    val newPath = updatePlus(op1.accessPath, tp)
    TreeInsertOperation(newPath, op1.tree, op1.id, context, op1.clientId)
  }

  private def transform(op1: TreeDeleteOperation, op2: TreeDeleteOperation, context: OperationContext): DataTypeOperation = {
    if (isEffectIndependent(op1.accessPath, op2.accessPath)) {
      return TreeDeleteOperation(op1.accessPath, op1.id, context, op1.clientId)
    }
    val tp = transformationPoint(op1.accessPath, op2.accessPath)
    if (op1.accessPath.list(tp) < op2.accessPath.list(tp)) {
      return TreeDeleteOperation(op1.accessPath, op1.id, context, op1.clientId)
    }
    if (op1.accessPath.list(tp) > op2.accessPath.list(tp)) {
      val newPath = updateMinus(op1.accessPath, tp)
      return TreeDeleteOperation(newPath, op1.id, context, op1.clientId)
    }
    if (op1.accessPath.list.size > op2.accessPath.list.size) {
      return TreeNoOperation(op1.id, context, op1.clientId)
    }
    if (op1.accessPath.list.size < op2.accessPath.list.size) {
      return TreeDeleteOperation(op1.accessPath, op1.id, context, op1.clientId)
    }
    TreeNoOperation(op1.id, context, op1.clientId)
  }

  private def transform(op1: TreeInsertOperation, op2: TreeDeleteOperation, context: OperationContext): DataTypeOperation = {
    if (isEffectIndependent(op1.accessPath, op2.accessPath)) {
      return TreeInsertOperation(op1.accessPath, op1.tree, op1.id, context, op1.clientId)
    }
    val tp = transformationPoint(op1.accessPath, op2.accessPath)
    if (op1.accessPath.list(tp) < op2.accessPath.list(tp)) {
      return TreeInsertOperation(op1.accessPath, op1.tree, op1.id, context, op1.clientId)
    }
    if (op1.accessPath.list(tp) > op2.accessPath.list(tp)) {
      val newPath = updateMinus(op1.accessPath, tp)
      return TreeInsertOperation(newPath, op1.tree, op1.id, context, op1.clientId)
    }
    if (op1.accessPath.list.size > op2.accessPath.list.size) {
      return TreeNoOperation(op1.id, context, op1.clientId)
    }
    TreeInsertOperation(op1.accessPath, op1.tree, op1.id, context, op1.clientId)
  }

  private def transform(op1: TreeDeleteOperation, op2: TreeInsertOperation, context: OperationContext): DataTypeOperation = {
    if (isEffectIndependent(op1.accessPath, op2.accessPath)) {
      return TreeDeleteOperation(op1.accessPath, op1.id, context, op1.clientId)
    }
    val tp = transformationPoint(op1.accessPath, op2.accessPath)
    if (op1.accessPath.list(tp) < op2.accessPath.list(tp)) {
      return TreeDeleteOperation(op1.accessPath, op1.id, context, op1.clientId)
    }
    if (op1.accessPath.list(tp) > op2.accessPath.list(tp)) {
      val newPath = updatePlus(op1.accessPath, tp)
      return TreeDeleteOperation(newPath, op1.id, context, op1.clientId)
    }
    if (op1.accessPath.list.size > op2.accessPath.list.size) {
      val newPath = updatePlus(op1.accessPath, tp)
      return TreeDeleteOperation(newPath, op1.id, context, op1.clientId)
    }
    TreeDeleteOperation(op1.accessPath, op1.id, context, op1.clientId)
  }
}

