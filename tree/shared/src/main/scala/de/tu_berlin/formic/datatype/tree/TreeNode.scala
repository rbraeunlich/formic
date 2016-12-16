package de.tu_berlin.formic.datatype.tree

import scala.collection.mutable.ArrayBuffer

/**
  * @author Ronny Bräunlich
  */
case class TreeNode(value: Any, children: ArrayBuffer[TreeNode] = ArrayBuffer.empty) {

  def getData(path: AccessPath): Any = {
    //in contrast to the application of an operation, we need an empty path here
    if(path.list.isEmpty){
      value
    } else {
      children(path.list.head).getData(path.dropFirstElement)
    }
  }


  def applyOperation(operation: TreeStructureOperation) = {
    if(!operation.isInstanceOf[TreeNoOperation]) {
      applyOperationInternal(operation, operation.accessPath)
    }
  }

  private def isCorrectLevel(accessPath: AccessPath): Boolean = {
    accessPath.list.length == 1
  }

  /**
    * Because the AccessPath within an operation should not be manipulated, this method takes an
    * explicit AccessPath which can be reduced at every level.
    *
    * @param operation  the operation to apply
    * @param accessPath the remaining access path
    */
  private def applyOperationInternal(operation: TreeStructureOperation, accessPath: AccessPath): Unit = {
    if (isCorrectLevel(accessPath)) {
      executeOperation(operation, accessPath.list.head)
    } else {
      children(accessPath.list.head).applyOperationInternal(operation, accessPath.dropFirstElement)
    }
  }

  private def executeOperation(operation: TreeStructureOperation, index: Int) = {
    operation match {
      case ins: TreeInsertOperation =>
        children.insert(index, ins.tree)
      case del: TreeDeleteOperation => children.remove(index)
      case no: TreeNoOperation =>
    }
  }
}
