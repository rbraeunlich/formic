package de.tu_berlin.formic.datatype.tree

/**
  * @author Ronny Bräunlich
  */
sealed trait TreeNode {
  def getData(path: AccessPath): Any

  def applyOperation(operation: TreeStructureOperation): TreeNode

  def getNode(path: AccessPath): TreeNode

  def getData: Any
}

case class ValueTreeNode(value: Any, children: List[ValueTreeNode] = List.empty) extends TreeNode {

  def getData(path: AccessPath): Any = {
    getNode(path).getData
  }


  override def getNode(path: AccessPath): TreeNode = {
    //in contrast to the application of an operation, we need an empty path here
    if (path.list.isEmpty) {
      this
    } else {
      children(path.list.head).getNode(path.dropFirstElement)
    }
  }



  def applyOperation(operation: TreeStructureOperation): ValueTreeNode = {
    if (!operation.isInstanceOf[TreeNoOperation]) {
      return applyOperationInternal(operation, operation.accessPath)
    }
    this
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
  private def applyOperationInternal(operation: TreeStructureOperation, accessPath: AccessPath): ValueTreeNode = {
    if (isCorrectLevel(accessPath)) {
      ValueTreeNode(value, executeOperation(operation, accessPath.list.head))
    } else {
      val child = children(accessPath.list.head)
      val newChild = child.applyOperationInternal(operation, accessPath.dropFirstElement)
      ValueTreeNode(value, children.updated(accessPath.list.head, newChild))
    }
  }

  private def executeOperation(operation: TreeStructureOperation, index: Int): List[ValueTreeNode] = {
    operation match {
      case ins: TreeInsertOperation =>
        val (front, back) = children.splitAt(index)
        front ++ List(ins.tree) ++ back
      case del: TreeDeleteOperation =>
        children.take(index) ++ children.drop(index + 1)
      case no: TreeNoOperation => children
    }
  }

  override def getData: Any = value
}

case object EmptyTreeNode extends TreeNode {
  override def applyOperation(operation: TreeStructureOperation): TreeNode = {
    operation match {
      case TreeInsertOperation(_, tree, _, _, _) => tree
      case _ => this
    }
  }

  override def getData(path: AccessPath): Any = null

  override def getNode(path: AccessPath): TreeNode = null

  override def getData: Any = null
}
