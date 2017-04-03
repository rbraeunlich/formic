package de.tu_berlin.formic.datastructure.tree

import de.tu_berlin.formic.common.datastructure.{DataStructureOperation, OperationContext, OperationTransformer}
import de.tu_berlin.formic.common.{ClientId, OperationId}

trait TreeStructureOperation extends DataStructureOperation {
  val accessPath: AccessPath
}

case class TreeInsertOperation(accessPath: AccessPath, tree: TreeNode, id: OperationId, operationContext: OperationContext, var clientId: ClientId) extends TreeStructureOperation

case class TreeDeleteOperation(accessPath: AccessPath, id: OperationId, operationContext: OperationContext, var clientId: ClientId) extends TreeStructureOperation

case class TreeNoOperation(id: OperationId, operationContext: OperationContext, var clientId: ClientId) extends TreeStructureOperation {
  override val accessPath: AccessPath = AccessPath(-1)
}

/**
  * @author Ronny Bräunlich
  */
class TreeTransformer extends OperationTransformer {

  def transformationPoint(path1: AccessPath, path2: AccessPath): Int = {
    val combined = path1.path.zip(path2.path).zipWithIndex
    val firstDifference = combined.find(threepel => threepel._1._1 != threepel._1._2)
    firstDifference.map(threepel => threepel._2).getOrElse(combined.size - 1)
  }

  def isEffectIndependent(path1: AccessPath, path2: AccessPath): Boolean = {
    val tp = transformationPoint(path1, path2)
    if (path1.path.size > tp + 1 && path2.path.size > tp + 1) true
    else if (path1.path(tp) > path2.path(tp) && path1.path.size < path2.path.size) true
    else if (path1.path(tp) < path2.path(tp) && path1.path.size > path2.path.size) true
    else false
  }

  override def transform(pair: (DataStructureOperation, DataStructureOperation)): DataStructureOperation = {
    transformInternal(pair, withNewContext = true)
  }

  protected def transformInternal(pair: (DataStructureOperation, DataStructureOperation), withNewContext: Boolean): DataStructureOperation = {
    val context = if (withNewContext) OperationContext(List(pair._2.id)) else pair._1.operationContext
    pair match {
      case (op1: TreeInsertOperation, op2: TreeInsertOperation) => transform(op1, op2, context)
      case (op1: TreeDeleteOperation, op2: TreeDeleteOperation) => transform(op1, op2, context)
      case (op1: TreeInsertOperation, op2: TreeDeleteOperation) => transform(op1, op2, context)
      case (op1: TreeDeleteOperation, op2: TreeInsertOperation) => transform(op1, op2, context)
      case (op1: TreeInsertOperation, op2: TreeNoOperation) => TreeInsertOperation(op1.accessPath, op1.tree, op1.id, context, op1.clientId)
      case (op1: TreeDeleteOperation, op2: TreeNoOperation) => TreeDeleteOperation(op1.accessPath, op1.id, context, op1.clientId)
      case (op1: TreeNoOperation, op2: TreeStructureOperation) => TreeNoOperation(op1.id, context, op1.clientId)
    }
  }


  protected def updatePlus(accessPath: AccessPath, tp: Int): AccessPath = {
    AccessPath(accessPath.path.zipWithIndex.map(t => if (t._2 == tp) t._1 + 1 else t._1):_*)
  }

  protected def updateMinus(accessPath: AccessPath, tp: Int): AccessPath = {
    AccessPath(accessPath.path.zipWithIndex.map(t => if (t._2 == tp) t._1 - 1 else t._1):_*)
  }

  private def transform(op1: TreeInsertOperation, op2: TreeInsertOperation, context: OperationContext): DataStructureOperation = {
    if (isEffectIndependent(op1.accessPath, op2.accessPath)) {
      return TreeInsertOperation(op1.accessPath, op1.tree, op1.id, context, op1.clientId)
    }
    val tp = transformationPoint(op1.accessPath, op2.accessPath)
    if (op1.accessPath.path(tp) < op2.accessPath.path(tp)) {
      return TreeInsertOperation(op1.accessPath, op1.tree, op1.id, context, op1.clientId)
    }
    if (op1.accessPath.path(tp) > op2.accessPath.path(tp)) {
      val newPath = updatePlus(op1.accessPath, tp)
      return TreeInsertOperation(newPath, op1.tree, op1.id, context, op1.clientId)
    }
    //access path at index tp must be equal
    if (op1.accessPath.path.size > op2.accessPath.path.size) {
      val newPath = updatePlus(op1.accessPath, tp)
      return TreeInsertOperation(newPath, op1.tree, op1.id, context, op1.clientId)
    }
    if (op1.accessPath.path.size < op2.accessPath.path.size) {
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

  private def transform(op1: TreeDeleteOperation, op2: TreeDeleteOperation, context: OperationContext): DataStructureOperation = {
    if (isEffectIndependent(op1.accessPath, op2.accessPath)) {
      return TreeDeleteOperation(op1.accessPath, op1.id, context, op1.clientId)
    }
    val tp = transformationPoint(op1.accessPath, op2.accessPath)
    if (op1.accessPath.path(tp) < op2.accessPath.path(tp)) {
      return TreeDeleteOperation(op1.accessPath, op1.id, context, op1.clientId)
    }
    if (op1.accessPath.path(tp) > op2.accessPath.path(tp)) {
      val newPath = updateMinus(op1.accessPath, tp)
      return TreeDeleteOperation(newPath, op1.id, context, op1.clientId)
    }
    if (op1.accessPath.path.size > op2.accessPath.path.size) {
      return TreeNoOperation(op1.id, context, op1.clientId)
    }
    if (op1.accessPath.path.size < op2.accessPath.path.size) {
      return TreeDeleteOperation(op1.accessPath, op1.id, context, op1.clientId)
    }
    TreeNoOperation(op1.id, context, op1.clientId)
  }

  private def transform(op1: TreeInsertOperation, op2: TreeDeleteOperation, context: OperationContext): DataStructureOperation = {
    if (isEffectIndependent(op1.accessPath, op2.accessPath)) {
      return TreeInsertOperation(op1.accessPath, op1.tree, op1.id, context, op1.clientId)
    }
    val tp = transformationPoint(op1.accessPath, op2.accessPath)
    if (op1.accessPath.path(tp) < op2.accessPath.path(tp)) {
      return TreeInsertOperation(op1.accessPath, op1.tree, op1.id, context, op1.clientId)
    }
    if (op1.accessPath.path(tp) > op2.accessPath.path(tp)) {
      val newPath = updateMinus(op1.accessPath, tp)
      return TreeInsertOperation(newPath, op1.tree, op1.id, context, op1.clientId)
    }
    if (op1.accessPath.path.size > op2.accessPath.path.size) {
      return TreeNoOperation(op1.id, context, op1.clientId)
    }
    TreeInsertOperation(op1.accessPath, op1.tree, op1.id, context, op1.clientId)
  }

  private def transform(op1: TreeDeleteOperation, op2: TreeInsertOperation, context: OperationContext): DataStructureOperation = {
    if (isEffectIndependent(op1.accessPath, op2.accessPath)) {
      return TreeDeleteOperation(op1.accessPath, op1.id, context, op1.clientId)
    }
    val tp = transformationPoint(op1.accessPath, op2.accessPath)
    if (op1.accessPath.path(tp) < op2.accessPath.path(tp)) {
      return TreeDeleteOperation(op1.accessPath, op1.id, context, op1.clientId)
    }
    if (op1.accessPath.path(tp) > op2.accessPath.path(tp)) {
      val newPath = updatePlus(op1.accessPath, tp)
      return TreeDeleteOperation(newPath, op1.id, context, op1.clientId)
    }
    if (op1.accessPath.path.size < op2.accessPath.path.size) {
      return TreeDeleteOperation(op1.accessPath, op1.id, context, op1.clientId)
    }
    val newPath = updatePlus(op1.accessPath, tp)
    TreeDeleteOperation(newPath, op1.id, context, op1.clientId)
  }
}

object TreeTransformer {
  def apply: TreeTransformer = new TreeTransformer()
}