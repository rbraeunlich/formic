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

  override def applyOperationRecursive(operation: TreeStructureOperation, accessPath: AccessPath): JsonTreeNode[T] = {
    throw new IllegalArgumentException(s"Illegal operation received. Atomic nodes do not apply operations: $operation")
  }
}

case class BooleanNode(key: String, private val value: Boolean) extends AtomicNode[Boolean] {

  override def getData: Boolean = value
}

case class NumberNode(key: String, private val value: Double) extends AtomicNode[Double] {

  override def getData: Double = value

}

case class CharacterNode(key: String, private val value: Char) extends AtomicNode[Char] {

  override def getData: Char = value

}

/**
  *
  * @tparam T the type of the contained children
  * @tparam V the type of the value this node contains
  */
trait JsonTreeNodeWithChildren[T <: JsonTreeNode[_], V] extends JsonTreeNode[V] {

  override val children: List[T]

  def executeOperation(operation: TreeStructureOperation, accessPath: AccessPath): List[T] = {
    val index = accessPath.path.head
    operation match {
      case TreeInsertOperation(_, tree: T, _, _, _) =>
        if (index > children.length) throw new ArrayIndexOutOfBoundsException
        val (front, back) = children.splitAt(index)
        front ++ List(tree) ++ back
      case del: TreeDeleteOperation =>
        if (index > children.length - 1) throw new ArrayIndexOutOfBoundsException
        children.take(index) ++ children.drop(index + 1)
      case rep: JsonReplaceOperation =>
        if (index > children.length - 1) throw new ArrayIndexOutOfBoundsException
        children.updated(index, rep.tree.asInstanceOf[T])
    }
  }
}

case class ArrayNode(key: String, children: List[JsonTreeNode[_]]) extends JsonTreeNodeWithChildren[JsonTreeNode[_], List[JsonTreeNode[_]]] {

  override def getData: List[JsonTreeNode[_]] = children

  private def isCorrectLevel(accessPath: AccessPath): Boolean = {
    accessPath.path.length == 1
  }

  override def applyOperationRecursive(operation: TreeStructureOperation, accessPath: AccessPath): ArrayNode = {
    if (isCorrectLevel(accessPath)) {
      ArrayNode(key, executeOperation(operation, accessPath))
    } else {
      val child = children(accessPath.path.head)
      val newChild = child.applyOperationRecursive(operation, accessPath.dropFirstElement)
      ArrayNode(key, children.updated(accessPath.path.head, newChild))
    }
  }
}

case class StringNode(key: String, children: List[CharacterNode]) extends JsonTreeNodeWithChildren[CharacterNode, String] {
  override def getData: String = children.map(c => c.getData).mkString

  private def isCorrectLevel(accessPath: AccessPath): Boolean = {
    accessPath.path.length == 1
  }

  override def applyOperationRecursive(operation: TreeStructureOperation, accessPath: AccessPath): StringNode = {
    if (isCorrectLevel(accessPath)) {
      StringNode(key, executeOperation(operation, accessPath))
    } else {
      throw new IllegalArgumentException("Character nodes cannot have children")
    }
  }
}

class ObjectNode private(val key: String, val children: List[JsonTreeNode[_]]) extends JsonTreeNode[List[JsonTreeNode[_]]] with Serializable {

  override def getData: List[JsonTreeNode[_]] = children

  private def isCorrectLevel(accessPath: AccessPath): Boolean = {
    accessPath.path.length == 1
  }

  override def applyOperationRecursive(operation: TreeStructureOperation, accessPath: AccessPath): JsonTreeNode[List[JsonTreeNode[_]]] = {
    if (isCorrectLevel(accessPath)) {
      ObjectNode(key, executeOperation(operation, accessPath))
    } else {
      val child = children(accessPath.path.head)
      val newChild = child.applyOperationRecursive(operation, accessPath.dropFirstElement)
      ObjectNode(key, children.updated(accessPath.path.head, newChild))
    }
  }

  def executeOperation(operation: TreeStructureOperation, accessPath: AccessPath): List[JsonTreeNode[_]] = {
    val index = accessPath.path.head
    operation match {
      case TreeInsertOperation(_, tree: JsonTreeNode[_], _, _, _) =>
        if (index > children.length) throw new ArrayIndexOutOfBoundsException
        if (children.map(child => child.key).contains(tree.key)) throw new IllegalArgumentException(s"Key ${tree.key} already occupied")
        val (front, back) = children.splitAt(index)
        front ++ List(tree) ++ back
      case del: TreeDeleteOperation =>
        if (index > children.length - 1) throw new ArrayIndexOutOfBoundsException
        children.take(index) ++ children.drop(index + 1)
      case rep: JsonReplaceOperation =>
        if (index > children.length - 1) throw new ArrayIndexOutOfBoundsException
        children.updated(index, rep.tree.asInstanceOf[JsonTreeNode[_]])
    }
  }

  def translateJsonPath(jsonPath: JsonPath): AccessPath = {
    AccessPath(translatePathRecursive(this, jsonPath): _*)
  }

  /**
    * A path for an insertion is allowed to point to a node that does not exist yet. Therefore it has to
    * be handled differently.
    */
  def translateJsonPathForInsertion(jsonPath: JsonPath): AccessPath = {
    val placeForInsertion = jsonPath.path.last
    val pathToParent = translateJsonPath(JsonPath(jsonPath.path.dropRight(1): _*))
    val parent = getNode(pathToParent)
    parent match {
      case ArrayNode(_, _) =>
        val newPath = pathToParent.path :+ placeForInsertion.toInt
        AccessPath(newPath: _*)
      case ObjectNode(_, nodes) =>
        val keyList = placeForInsertion :: nodes.map(node => node.key)
        val newPath = pathToParent.path :+ keyList.sorted.zipWithIndex.find(t => t._1 == placeForInsertion).get._2
        AccessPath(newPath: _*)
    }
  }

  def translatePathRecursive(node: JsonTreeNode[_], jsonPath: JsonPath): List[Int] = {
    if (jsonPath.path.isEmpty) return List.empty

    val childWithIndex = node.children.zipWithIndex.find(t => t._1.key == jsonPath.path.head)
    childWithIndex match {
      case Some(c) => List(c._2) ::: translatePathRecursive(c._1, jsonPath.dropFirstElement)
      case None =>
        //within an arraynode the no keys are present, only the indices
        if (jsonPath.path.length == 1 && node.isInstanceOf[ArrayNode]) {
          val index = jsonPath.path.head.toInt
          if (index < node.asInstanceOf[ArrayNode].children.length) return List(index)
        }
        throw new IllegalArgumentException(s"Illegal JSON Path encountered: $jsonPath for node $node")
    }
  }

  override def toString = s"ObjectNode($key, $children)"

  def canEqual(other: Any): Boolean = other.isInstanceOf[ObjectNode]

  override def equals(other: Any): Boolean = other match {
    case that: ObjectNode =>
      (that canEqual this) &&
        key == that.key &&
        children == that.children
    case _ => false
  }

  override def hashCode(): Int = {
    val state = Seq(key, children)
    state.map(_.hashCode()).foldLeft(0)((a, b) => 31 * a + b)
  }
}

object ObjectNode {
  def apply(key: String, children: List[JsonTreeNode[_]]): ObjectNode = new ObjectNode(key, children.sortBy(_.key))

  def unapply(arg: ObjectNode): Option[(String, List[JsonTreeNode[_]])] = Some(arg.key, arg.children)
}

