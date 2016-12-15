package de.tu_berlin.formic.datatype.tree

import de.tu_berlin.formic.common.datatype.OperationContext
import de.tu_berlin.formic.common.{ClientId, OperationId}
import org.scalatest.{FlatSpec, Matchers}

import scala.collection.mutable.ArrayBuffer

/**
  * @author Ronny Bräunlich
  */
class TreeNodeSpec extends FlatSpec with Matchers {

  "A TreeNode" should "apply an insertion a the correct level" in {
    val leaf = TreeNode("0")
    val leftChild = TreeNode("a", ArrayBuffer(leaf))
    val rightChild = TreeNode("b")
    val root = TreeNode("X", ArrayBuffer(leftChild, rightChild))
    val toInsert = TreeNode("Q")
    val operation = TreeInsertOperation(AccessPath(0, 1), toInsert, OperationId(), OperationContext(), ClientId())

    root.applyOperation(operation)

    root.children(0).children(1) should equal(toInsert)
  }

  it should "accept a node below the root" in {
    val root = TreeNode("X")
    val toInsert = TreeNode("Q")
    val operation = TreeInsertOperation(AccessPath(0), toInsert, OperationId(), OperationContext(), ClientId())

    root.applyOperation(operation)

    root.children(0) should equal(toInsert)
  }

  it should "apply a deletion at the correct level" in {
    val leaf = TreeNode("0")
    val leaf2 = TreeNode("1")
    val leftChild = TreeNode("a", ArrayBuffer(leaf, leaf2))
    val rightChild = TreeNode("b")
    val root = TreeNode("X", ArrayBuffer(leftChild, rightChild))
    val operation = TreeDeleteOperation(AccessPath(0), OperationId(), OperationContext(), ClientId())

    root.applyOperation(operation)

    root.children(0) should equal(rightChild)
  }

  it should "ignore a no operation" in {
    val leftChild = TreeNode("a")
    val rightChild = TreeNode("b")
    val root = TreeNode("X", ArrayBuffer(leftChild, rightChild))
    val operation = TreeNoOperation(AccessPath(0), OperationId(), OperationContext(), ClientId())

    root.applyOperation(operation)

    root.children(0) should equal(leftChild)
    root.children(1) should equal(rightChild)
  }

  it should "retrieve the correct data" in {
    val leaf = TreeNode("0")
    val leftChild = TreeNode("a", ArrayBuffer(leaf))
    val rightChild = TreeNode("b")
    val root = TreeNode("X", ArrayBuffer(leftChild, rightChild))

    root.getData(AccessPath(1)) should equal("b")
  }

  it should "get the data from the root" in {
    val root = TreeNode("X")

    root.getData(AccessPath()) should equal("X")
  }
}
