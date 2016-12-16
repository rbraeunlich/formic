package de.tu_berlin.formic.datatype.tree

import de.tu_berlin.formic.common.datatype.OperationContext
import de.tu_berlin.formic.common.{ClientId, OperationId}
import org.scalatest.{FlatSpec, Matchers}

/**
  * @author Ronny Bräunlich
  */
class TreeNodeSpec extends FlatSpec with Matchers {

  "A ValueTreeNode" should "apply an insertion a the correct level" in {
    val leaf = ValueTreeNode("0")
    val leftChild = ValueTreeNode("a", List(leaf))
    val rightChild = ValueTreeNode("b")
    val root = ValueTreeNode("X", List(leftChild, rightChild))
    val toInsert = ValueTreeNode("Q")
    val operation = TreeInsertOperation(AccessPath(0, 1), toInsert, OperationId(), OperationContext(), ClientId())

    val result = root.applyOperation(operation)

    result.children.head.children(1) should equal(toInsert)
  }

  it should "accept a node below the root" in {
    val root = ValueTreeNode("X")
    val toInsert = ValueTreeNode("Q")
    val operation = TreeInsertOperation(AccessPath(0), toInsert, OperationId(), OperationContext(), ClientId())

    val result = root.applyOperation(operation)

    result.children.head should equal(toInsert)
  }

  it should "apply a deletion at the correct level" in {
    val leaf = ValueTreeNode("0")
    val leaf2 = ValueTreeNode("1")
    val leftChild = ValueTreeNode("a", List(leaf, leaf2))
    val rightChild = ValueTreeNode("b")
    val root = ValueTreeNode("X", List(leftChild, rightChild))
    val operation = TreeDeleteOperation(AccessPath(0), OperationId(), OperationContext(), ClientId())

    val result = root.applyOperation(operation)

    result.children.head should equal(rightChild)
  }

  it should "ignore a no operation" in {
    val leftChild = ValueTreeNode("a")
    val rightChild = ValueTreeNode("b")
    val root = ValueTreeNode("X", List(leftChild, rightChild))
    val operation = TreeNoOperation(AccessPath(0), OperationId(), OperationContext(), ClientId())

    val result = root.applyOperation(operation)

    result.children.head should equal(leftChild)
    root.children(1) should equal(rightChild)
  }

  it should "retrieve the correct data" in {
    val leaf = ValueTreeNode("0")
    val leftChild = ValueTreeNode("a", List(leaf))
    val rightChild = ValueTreeNode("b")
    val root = ValueTreeNode("X", List(leftChild, rightChild))

    root.getData(AccessPath(1)) should equal("b")
  }

  it should "get the data from the root" in {
    val root = ValueTreeNode("X")

    root.getData(AccessPath()) should equal("X")
  }

  "An EmptyTreeNode" should "replace itself with insert operation" in {
    val tree = ValueTreeNode(true)
    val operation = TreeInsertOperation(AccessPath(0, 1), tree, OperationId(), OperationContext(), ClientId())

    val result = EmptyTreeNode.applyOperation(operation)

    result should equal(tree)
  }

  it should "ignore delete operations" in {
    val result = EmptyTreeNode.applyOperation(TreeDeleteOperation(AccessPath(0), OperationId(), OperationContext(), ClientId()))

    result should equal(EmptyTreeNode)
  }

  it should "ignore no operations" in {
    val result = EmptyTreeNode.applyOperation(TreeNoOperation(AccessPath(0), OperationId(), OperationContext(), ClientId()))

    result should equal(EmptyTreeNode)
  }
}
