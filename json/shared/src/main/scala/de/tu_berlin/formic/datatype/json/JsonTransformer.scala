package de.tu_berlin.formic.datatype.json

import de.tu_berlin.formic.common.datatype.{DataTypeOperation, OperationContext}
import de.tu_berlin.formic.common.{ClientId, OperationId}
import de.tu_berlin.formic.datatype.tree._

/**
  * @author Ronny Bräunlich
  */

case class JsonReplaceOperation(accessPath: AccessPath, tree: TreeNode, id: OperationId, operationContext: OperationContext, var clientId: ClientId) extends TreeStructureOperation

class JsonTransformer extends TreeTransformer {

  override def transform(pair: (DataTypeOperation, DataTypeOperation)): DataTypeOperation = {
    transformInternal(pair, withNewContext = true)
  }

  override protected def transformInternal(pair: (DataTypeOperation, DataTypeOperation), withNewContext: Boolean): DataTypeOperation = {
    val context = if (withNewContext) OperationContext(List(pair._2.id)) else pair._1.operationContext
    pair match {
      case (op1: JsonReplaceOperation, op2: JsonReplaceOperation) => transform(op1, op2, context)
      case (op1: JsonReplaceOperation, op2: TreeInsertOperation) => transform(op1, op2, context)
      case (op1: JsonReplaceOperation, op2: TreeDeleteOperation) => transform(op1, op2, context)
      case (TreeInsertOperation(path, tree, id, _, clientId), _: JsonReplaceOperation) => TreeInsertOperation(path, tree, id, context, clientId) //only have to change the context here
      case (TreeDeleteOperation(path, id, _, clientId), _: JsonReplaceOperation) => TreeDeleteOperation(path, id, context, clientId) //only have to change the context here
      case (_, _) => super.transformInternal(pair, withNewContext)
    }
  }

  private def transform(op1: JsonReplaceOperation, op2: JsonReplaceOperation, context: OperationContext): DataTypeOperation = {
    if (op1.accessPath != op2.accessPath) {
      return JsonReplaceOperation(op1.accessPath, op1.tree, op1.id, context, op1.clientId)
    }
    if (op1.tree == op2.tree) {
      return TreeNoOperation(op1.id, context, op1.clientId)
    }
    if (op1.clientId < op2.clientId) {
      return TreeNoOperation(op1.id, context, op1.clientId)
    }
    JsonReplaceOperation(op1.accessPath, op1.tree, op1.id, context, op1.clientId)
  }

  private def transform(op1: JsonReplaceOperation, op2: TreeInsertOperation, context: OperationContext): DataTypeOperation = {
    if (isEffectIndependent(op1.accessPath, op2.accessPath)) {
      return JsonReplaceOperation(op1.accessPath, op1.tree, op1.id, context, op1.clientId)
    }
    val tp = transformationPoint(op1.accessPath, op2.accessPath)
    if (op1.accessPath.path(tp) < op2.accessPath.path(tp)) {
      return JsonReplaceOperation(op1.accessPath, op1.tree, op1.id, context, op1.clientId)
    }
    if (op1.accessPath.path(tp) > op2.accessPath.path(tp)) {
      val newPath = updatePlus(op1.accessPath, tp)
      return JsonReplaceOperation(newPath, op1.tree, op1.id, context, op1.clientId)
    }
    if (op1.accessPath.path.size > op2.accessPath.path.size) {
      val newPath = updatePlus(op1.accessPath, tp)
      return JsonReplaceOperation(newPath, op1.tree, op1.id, context, op1.clientId)
    }
    JsonReplaceOperation(op1.accessPath, op1.tree, op1.id, context, op1.clientId)
  }

  private def transform(op1: JsonReplaceOperation, op2: TreeDeleteOperation, context: OperationContext): DataTypeOperation = {
    if (isEffectIndependent(op1.accessPath, op2.accessPath)) {
      return JsonReplaceOperation(op1.accessPath, op1.tree, op1.id, context, op1.clientId)
    }
    val tp = transformationPoint(op1.accessPath, op2.accessPath)
    if (op1.accessPath.path(tp) < op2.accessPath.path(tp)) {
      return JsonReplaceOperation(op1.accessPath, op1.tree, op1.id, context, op1.clientId)
    }
    if (op1.accessPath.path(tp) > op2.accessPath.path(tp)) {
      val newPath = updateMinus(op1.accessPath, tp)
      return JsonReplaceOperation(newPath, op1.tree, op1.id, context, op1.clientId)
    }
    if (op1.accessPath.path.size > op2.accessPath.path.size) {
      val newPath = updateMinus(op1.accessPath, tp)
      return JsonReplaceOperation(newPath, op1.tree, op1.id, context, op1.clientId)
    }
    JsonReplaceOperation(op1.accessPath, op1.tree, op1.id, context, op1.clientId)
  }
}
