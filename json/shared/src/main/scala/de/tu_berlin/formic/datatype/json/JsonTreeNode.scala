package de.tu_berlin.formic.datatype.json

import de.tu_berlin.formic.datatype.tree._

/**
  * @author Ronny Bräunlich
  */
trait JsonTreeNode[T] extends TreeNode {
  val key: String

  override def getData: T

  override val children: List[JsonTreeNode[_]]

  override def applyOperationRecursive(operation: TreeStructureOperation, accessPath: AccessPath): JsonTreeNode[T]

}

abstract class AtomicNode[T] extends JsonTreeNode[T] {
  override val children: List[JsonTreeNode[_]] = List.empty
}

case class BooleanNode(key: String, private val value: Boolean) extends AtomicNode[Boolean] {

  override def getData: Boolean = value


  override def applyOperationRecursive(operation: TreeStructureOperation, accessPath: AccessPath): BooleanNode = {
    if (accessPath.list.nonEmpty) throw new IllegalArgumentException("Atomic node cannot have children")
    operation match {
      case JsonReplaceOperation(_, newNode: BooleanNode, _, _, _) => newNode
      case _ => throw new IllegalArgumentException(s"Illegal operation received: $operation")
    }
  }
}

case class NumberNode(key: String, private val value: Double) extends AtomicNode[Double] {

  override def getData: Double = value

  override def applyOperationRecursive(operation: TreeStructureOperation, accessPath: AccessPath): NumberNode = {
    if (accessPath.list.nonEmpty) throw new IllegalArgumentException("Atomic node cannot have children")
    operation match {
      case JsonReplaceOperation(_, newNode: NumberNode, _, _, _) => newNode
      case _ => throw new IllegalArgumentException(s"Illegal operation received: $operation")
    }
  }
}

case class CharacterNode(key: String, private val value: Char) extends AtomicNode[Char] {
  override def getData: Char = value

  override def applyOperationRecursive(operation: TreeStructureOperation, accessPath: AccessPath): CharacterNode = {
    //slightly dirty but all Char changes should be done by StringNode not the CharacterNode itself
    throw new IllegalArgumentException(s"Illegal operation received: $operation")
  }
}

case class ArrayNode(key: String, children: List[JsonTreeNode[_]]) extends JsonTreeNode[List[JsonTreeNode[_]]] {

  override def getData: List[JsonTreeNode[_]] = children

  private def isCorrectLevel(accessPath: AccessPath): Boolean = {
    accessPath.list.length == 1
  }

  override def applyOperationRecursive(operation: TreeStructureOperation, accessPath: AccessPath): ArrayNode = {
    if (isCorrectLevel(accessPath)) {
      ArrayNode(key, executeOperation(operation, accessPath))
    } else {
      val child = children(accessPath.list.head)
      val newChild = child.applyOperationRecursive(operation, accessPath.dropFirstElement)
      ArrayNode(key, children.updated(accessPath.list.head, newChild))
    }
  }

  private def executeOperation(operation: TreeStructureOperation, accessPath: AccessPath): List[JsonTreeNode[_]] = {
    val index = accessPath.list.head
    operation match {
      case TreeInsertOperation(_, tree: JsonTreeNode[_], _, _, _) =>
        if(index > children.length) throw new ArrayIndexOutOfBoundsException
        val (front, back) = children.splitAt(index)
        front ++ List(tree) ++ back
      case del: TreeDeleteOperation =>
        if(index > children.length - 1) throw new ArrayIndexOutOfBoundsException
        children.take(index) ++ children.drop(index + 1)
      case rep: JsonReplaceOperation =>
        if(index > children.length - 1) throw new ArrayIndexOutOfBoundsException
        children.updated(index, children(index).applyOperationRecursive(rep, accessPath.dropFirstElement))
    }
  }

}

case class StringNode(key: String, children: List[CharacterNode]) extends JsonTreeNode[String] {
  override def getData: String = children.map(c => c.getData).mkString

  private def isCorrectLevel(accessPath: AccessPath): Boolean = {
    accessPath.list.length == 1
  }

  override def applyOperationRecursive(operation: TreeStructureOperation, accessPath: AccessPath): StringNode = {
    if (isCorrectLevel(accessPath)) {
      StringNode(key, executeOperation(operation, accessPath))
    } else {
      throw new IllegalArgumentException("Character nodes cannot have children")
    }
  }

  private def executeOperation(operation: TreeStructureOperation, accessPath: AccessPath): List[CharacterNode] = {
    val index = accessPath.list.head
    if(index > children.length) throw new ArrayIndexOutOfBoundsException
    operation match {
      case TreeInsertOperation(_, char: CharacterNode, _, _, _) =>
        if(index > children.length) throw new ArrayIndexOutOfBoundsException
        val (front, back) = children.splitAt(index)
        front ++ List(char) ++ back
      case del: TreeDeleteOperation =>
        if(index > children.length - 1) throw new ArrayIndexOutOfBoundsException
        children.take(index) ++ children.drop(index + 1)
      case rep: JsonReplaceOperation =>
        if(index > children.length - 1) throw new ArrayIndexOutOfBoundsException
        children.updated(index, rep.tree.asInstanceOf[CharacterNode])
    }
  }
}

