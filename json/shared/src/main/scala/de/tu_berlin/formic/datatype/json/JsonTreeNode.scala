package de.tu_berlin.formic.datatype.json

import de.tu_berlin.formic.datatype.tree.{AccessPath, TreeNode, TreeStructureOperation}

/**
  * @author Ronny Bräunlich
  */
trait JsonTreeNode[T] extends TreeNode {
  val key: String

  override def getData: T

  override val children: List[JsonTreeNode[_]]
}

abstract class AtomicNode[T] extends JsonTreeNode[T] {
  override val children: List[JsonTreeNode[_]] = List.empty
}

case class BooleanNode(key: String, private val value: Boolean) extends AtomicNode[Boolean] {

  override def getData: Boolean = value


  override protected def applyOperationInternal(operation: TreeStructureOperation, accessPath: AccessPath): TreeNode = ???
}

case class NumberNode(key: String, private val value: Double) extends AtomicNode[Double] {

  override def getData: Double = value

  override protected def applyOperationInternal(operation: TreeStructureOperation, accessPath: AccessPath): TreeNode = ???
}

case class CharacterNode(key: String, private val value: Char) extends AtomicNode[Char] {
  override def getData: Char = value

  override protected def applyOperationInternal(operation: TreeStructureOperation, accessPath: AccessPath): TreeNode = ???
}