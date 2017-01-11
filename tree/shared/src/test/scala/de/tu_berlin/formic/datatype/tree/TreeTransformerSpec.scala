package de.tu_berlin.formic.datatype.tree

import de.tu_berlin.formic.common.datatype.OperationContext
import de.tu_berlin.formic.common.{ClientId, OperationId}
import org.scalatest.{FlatSpec, Matchers}

/**
  * @author Ronny Bräunlich
  */
class TreeTransformerSpec extends FlatSpec with Matchers {

  "A TreeTransformer" should "calculate the correct transformation point for two independent paths" in {
    val path1 = AccessPath(5)
    val path2 = AccessPath(1, 2, 3)

    new TreeTransformer().transformationPoint(path1, path2) should be(0)
  }

  it should "calculate correct transformation point when first path is contained in second" in {
    val path1 = AccessPath(1, 2)
    val path2 = AccessPath(1, 2, 3)

    new TreeTransformer().transformationPoint(path1, path2) should be(1)
  }

  it should "calculate correct transformation point when second path is contained in first" in {
    val path1 = AccessPath(5, 6, 7, 8, 9)
    val path2 = AccessPath(5, 6, 7)

    new TreeTransformer().transformationPoint(path1, path2) should be(2)
  }

  it should "calculate correct transformation point for two equal paths" in {
    val path1 = AccessPath(5, 6, 7, 8, 9)
    val path2 = AccessPath(5, 6, 7, 8, 9)

    new TreeTransformer().transformationPoint(path1, path2) should be(4)
  }

  it should "state effect independence if paths point to two different subtrees" in {
    val path1 = AccessPath(0, 0)
    val path2 = AccessPath(1, 0)

    new TreeTransformer().isEffectIndependent(path1, path2) should be(true)
  }

  it should "state effect independence if first path is uncle of second" in {
    val path1 = AccessPath(1)
    val path2 = AccessPath(0, 0)

    new TreeTransformer().isEffectIndependent(path1, path2) should be(true)
  }

  it should "state effect independence if second path is uncle of first" in {
    val path1 = AccessPath(0, 0)
    val path2 = AccessPath(1)

    new TreeTransformer().isEffectIndependent(path1, path2) should be(true)
  }

  it should "state effect dependence if paths point to siblings" in {
    val path1 = AccessPath(0, 0)
    val path2 = AccessPath(0, 1)

    new TreeTransformer().isEffectIndependent(path1, path2) should be(false)
  }

  it should "not change first insert operation when the two are effect independent" in {
    val path1 = AccessPath(0, 0)
    val path2 = AccessPath(1, 0)
    val op1 = TreeInsertOperation(path1, ValueTreeNode("Foo"), OperationId(), OperationContext(), ClientId())
    val op2 = TreeInsertOperation(path2, ValueTreeNode("Bar"), OperationId(), OperationContext(), ClientId())

    val transformed = new TreeTransformer().transform((op1, op2))

    transformed should equal(TreeInsertOperation(op1.accessPath, op1.tree, op1.id, OperationContext(List(op2.id)), op1.clientId))
  }

  it should "not change first insert operation when the index value at the transformation point is smaller" in {
    val path1 = AccessPath(0, 0)
    val path2 = AccessPath(0, 1)
    val op1 = TreeInsertOperation(path1, ValueTreeNode("Foo"), OperationId(), OperationContext(), ClientId())
    val op2 = TreeInsertOperation(path2, ValueTreeNode("Bar"), OperationId(), OperationContext(), ClientId())

    val transformed = new TreeTransformer().transform((op1, op2))

    transformed should equal(TreeInsertOperation(op1.accessPath, op1.tree, op1.id, OperationContext(List(op2.id)), op1.clientId))
  }

  it should "increment the index at the transformation point of the first insert operation when its index is higher" in {
    val path1 = AccessPath(0, 2)
    val path2 = AccessPath(0, 1)
    val op1 = TreeInsertOperation(path1, ValueTreeNode("Foo"), OperationId(), OperationContext(), ClientId())
    val op2 = TreeInsertOperation(path2, ValueTreeNode("Bar"), OperationId(), OperationContext(), ClientId())

    val transformed = new TreeTransformer().transform((op1, op2))

    transformed should equal(TreeInsertOperation(AccessPath(0, 3), op1.tree, op1.id, OperationContext(List(op2.id)), op1.clientId))
  }

  it should "increment the index at the transformation point of the first insert operation if the paths are equal at the transformation point and the path is longer" in {
    val path1 = AccessPath(0, 1, 0)
    val path2 = AccessPath(0, 1)
    val op1 = TreeInsertOperation(path1, ValueTreeNode("Foo"), OperationId(), OperationContext(), ClientId())
    val op2 = TreeInsertOperation(path2, ValueTreeNode("Bar"), OperationId(), OperationContext(), ClientId())

    val transformed = new TreeTransformer().transform((op1, op2))

    transformed should equal(TreeInsertOperation(AccessPath(0, 2, 0), op1.tree, op1.id, OperationContext(List(op2.id)), op1.clientId))
  }

  it should "not change the first insert operation when the paths are equal at the transformation point and the path is shorter" in {
    val path1 = AccessPath(0, 1)
    val path2 = AccessPath(0, 1, 0)
    val op1 = TreeInsertOperation(path1, ValueTreeNode("Foo"), OperationId(), OperationContext(), ClientId())
    val op2 = TreeInsertOperation(path2, ValueTreeNode("Bar"), OperationId(), OperationContext(), ClientId())

    val transformed = new TreeTransformer().transform((op1, op2))

    transformed should equal(TreeInsertOperation(op1.accessPath, op1.tree, op1.id, OperationContext(List(op2.id)), op1.clientId))
  }

  it should "return no-operation if both access paths are equal and the trees too" in {
    val path1 = AccessPath(0, 2, 1)
    val path2 = AccessPath(0, 2, 1)
    val op1 = TreeInsertOperation(path1, ValueTreeNode("Foo"), OperationId(), OperationContext(), ClientId())
    val op2 = TreeInsertOperation(path2, ValueTreeNode("Foo"), OperationId(), OperationContext(), ClientId())

    val transformed = new TreeTransformer().transform((op1, op2))

    transformed should equal(TreeNoOperation(op1.id, OperationContext(List(op2.id)), op1.clientId))
  }

  it should "not change the first insert operation when both access paths are equal, the trees not and the client id is bigger" in {
    val path1 = AccessPath(0, 2, 1)
    val path2 = AccessPath(0, 2, 1)
    val op1 = TreeInsertOperation(path1, ValueTreeNode("Foo"), OperationId(), OperationContext(), ClientId("2"))
    val op2 = TreeInsertOperation(path2, ValueTreeNode("Bar"), OperationId(), OperationContext(), ClientId("1"))

    val transformed = new TreeTransformer().transform((op1, op2))

    transformed should equal(TreeInsertOperation(op1.accessPath, op1.tree, op1.id, OperationContext(List(op2.id)), op1.clientId))
  }

  it should "increment the index at the transformation point of the first insert operation when both access paths are equal, the trees not and the client id is smaller" in {
    val path1 = AccessPath(0, 2, 1)
    val path2 = AccessPath(0, 2, 1)
    val op1 = TreeInsertOperation(path1, ValueTreeNode("Foo"), OperationId(), OperationContext(), ClientId("1"))
    val op2 = TreeInsertOperation(path2, ValueTreeNode("Bar"), OperationId(), OperationContext(), ClientId("2"))

    val transformed = new TreeTransformer().transform((op1, op2))

    transformed should equal(TreeInsertOperation(AccessPath(0, 2, 2), op1.tree, op1.id, OperationContext(List(op2.id)), op1.clientId))
  }

  it should "not change first delete operation when the two are effect independent" in {
    val path1 = AccessPath(0, 0)
    val path2 = AccessPath(1, 0)
    val op1 = TreeDeleteOperation(path1, OperationId(), OperationContext(), ClientId())
    val op2 = TreeDeleteOperation(path2, OperationId(), OperationContext(), ClientId())

    val transformed = new TreeTransformer().transform((op1, op2))

    transformed should equal(TreeDeleteOperation(op1.accessPath, op1.id, OperationContext(List(op2.id)), op1.clientId))
  }

  it should "not change first delete operation when the index value at the transformation point is smaller" in {
    val path1 = AccessPath(0, 0)
    val path2 = AccessPath(0, 1)
    val op1 = TreeDeleteOperation(path1, OperationId(), OperationContext(), ClientId())
    val op2 = TreeDeleteOperation(path2, OperationId(), OperationContext(), ClientId())

    val transformed = new TreeTransformer().transform((op1, op2))

    transformed should equal(TreeDeleteOperation(op1.accessPath, op1.id, OperationContext(List(op2.id)), op1.clientId))
  }

  it should "decrement the index at the transformation point of the first delete operation when its index is higher" in {
    val path1 = AccessPath(0, 2)
    val path2 = AccessPath(0, 1)
    val op1 = TreeDeleteOperation(path1, OperationId(), OperationContext(), ClientId())
    val op2 = TreeDeleteOperation(path2, OperationId(), OperationContext(), ClientId())

    val transformed = new TreeTransformer().transform((op1, op2))

    transformed should equal(TreeDeleteOperation(AccessPath(0, 1), op1.id, OperationContext(List(op2.id)), op1.clientId))
  }

  it should "return no-operation when the index at the transformation point is equal and the path of the first operation longer" in {
    val path1 = AccessPath(0, 2, 1)
    val path2 = AccessPath(0, 2)
    val op1 = TreeDeleteOperation(path1, OperationId(), OperationContext(), ClientId())
    val op2 = TreeDeleteOperation(path2, OperationId(), OperationContext(), ClientId())

    val transformed = new TreeTransformer().transform((op1, op2))

    transformed should equal(TreeNoOperation(op1.id, OperationContext(List(op2.id)), op1.clientId))
  }

  it should "not change the first delete operation when the index at the transformation point is equal and the path of the first operation shorter" in {
    val path1 = AccessPath(0, 3)
    val path2 = AccessPath(0, 3, 5, 6)
    val op1 = TreeDeleteOperation(path1, OperationId(), OperationContext(), ClientId())
    val op2 = TreeDeleteOperation(path2, OperationId(), OperationContext(), ClientId())

    val transformed = new TreeTransformer().transform((op1, op2))

    transformed should equal(TreeDeleteOperation(op1.accessPath, op1.id, OperationContext(List(op2.id)), op1.clientId))
  }

  it should "return no-operation when both paths are equal" in {
    val path1 = AccessPath(0, 2, 1)
    val path2 = AccessPath(0, 2, 1)
    val op1 = TreeDeleteOperation(path1, OperationId(), OperationContext(), ClientId())
    val op2 = TreeDeleteOperation(path2, OperationId(), OperationContext(), ClientId())

    val transformed = new TreeTransformer().transform((op1, op2))

    transformed should equal(TreeNoOperation(op1.id, OperationContext(List(op2.id)), op1.clientId))
  }

  it should "not change the insert operation when it is effect independent with the delete operation" in {
    val path1 = AccessPath(0, 2, 1)
    val path2 = AccessPath(1, 2, 1)
    val op1 = TreeInsertOperation(path1, ValueTreeNode("abc"), OperationId(), OperationContext(), ClientId())
    val op2 = TreeDeleteOperation(path2, OperationId(), OperationContext(), ClientId())

    val transformed = new TreeTransformer().transform((op1, op2))

    transformed should equal(TreeInsertOperation(op1.accessPath, op1.tree, op1.id, OperationContext(List(op2.id)), op1.clientId))
  }

  it should "not change the insert operation when its index at the transformation point is smaller than the one from the delete operation" in {
    val path1 = AccessPath(0, 2)
    val path2 = AccessPath(0, 3)
    val op1 = TreeInsertOperation(path1, ValueTreeNode("abc"), OperationId(), OperationContext(), ClientId())
    val op2 = TreeDeleteOperation(path2, OperationId(), OperationContext(), ClientId())

    val transformed = new TreeTransformer().transform((op1, op2))

    transformed should equal(TreeInsertOperation(op1.accessPath, op1.tree, op1.id, OperationContext(List(op2.id)), op1.clientId))
  }

  it should "decrease the index at the transformation point when the index of the insert at the transformation point is greater than the one from the delete operation" in {
    val path1 = AccessPath(0, 3)
    val path2 = AccessPath(0, 2)
    val op1 = TreeInsertOperation(path1, ValueTreeNode("abc"), OperationId(), OperationContext(), ClientId())
    val op2 = TreeDeleteOperation(path2, OperationId(), OperationContext(), ClientId())

    val transformed = new TreeTransformer().transform((op1, op2))

    transformed should equal(TreeInsertOperation(AccessPath(0, 2), op1.tree, op1.id, OperationContext(List(op2.id)), op1.clientId))
  }

  it should "return the no operation when the index at the transformation point is equal to the one of the delete operation and its path is longer" in {
    val path1 = AccessPath(0, 2, 1)
    val path2 = AccessPath(0, 2)
    val op1 = TreeInsertOperation(path1, ValueTreeNode("abc"), OperationId(), OperationContext(), ClientId())
    val op2 = TreeDeleteOperation(path2, OperationId(), OperationContext(), ClientId())

    val transformed = new TreeTransformer().transform((op1, op2))

    transformed should equal(TreeNoOperation(op1.id, OperationContext(List(op2.id)), op1.clientId))
  }

  it should "not change the insert operation when the index at the transformation point is equal to the one of the delete operation and its path is shorter" in {
    val path1 = AccessPath(0, 2)
    val path2 = AccessPath(0, 2, 1)
    val op1 = TreeInsertOperation(path1, ValueTreeNode("abc"), OperationId(), OperationContext(), ClientId())
    val op2 = TreeDeleteOperation(path2, OperationId(), OperationContext(), ClientId())

    val transformed = new TreeTransformer().transform((op1, op2))

    transformed should equal(TreeInsertOperation(op1.accessPath, op1.tree, op1.id, OperationContext(List(op2.id)), op1.clientId))
  }

  it should "not change the delete operation when it is effect independent with the insert operation" in {
    val path1 = AccessPath(0, 2, 1)
    val path2 = AccessPath(1, 2, 1)
    val op1 = TreeDeleteOperation(path1, OperationId(), OperationContext(), ClientId())
    val op2 = TreeInsertOperation(path2, ValueTreeNode("abc"), OperationId(), OperationContext(), ClientId())

    val transformed = new TreeTransformer().transform((op1, op2))

    transformed should equal(TreeDeleteOperation(op1.accessPath, op1.id, OperationContext(List(op2.id)), op1.clientId))
  }

  it should "not change the delete operation when its index at the transformation point is smaller than the one from the insert operation" in {
    val path1 = AccessPath(0, 2)
    val path2 = AccessPath(0, 3)
    val op1 = TreeDeleteOperation(path1, OperationId(), OperationContext(), ClientId())
    val op2 = TreeInsertOperation(path2, ValueTreeNode("abc"), OperationId(), OperationContext(), ClientId())


    val transformed = new TreeTransformer().transform((op1, op2))

    transformed should equal(TreeDeleteOperation(op1.accessPath, op1.id, OperationContext(List(op2.id)), op1.clientId))
  }

  it should "increase the index at the transformation point when the index of the delete at the transformation point is greater than the one from the insert operation" in {
    val path1 = AccessPath(0, 3)
    val path2 = AccessPath(0, 2)
    val op1 = TreeDeleteOperation(path1, OperationId(), OperationContext(), ClientId())
    val op2 = TreeInsertOperation(path2, ValueTreeNode("abc"), OperationId(), OperationContext(), ClientId())

    val transformed = new TreeTransformer().transform((op1, op2))

    transformed should equal(TreeDeleteOperation(AccessPath(0, 4), op1.id, OperationContext(List(op2.id)), op1.clientId))
  }

  it should "increase the index at the transformation point when the index at the transformation point is equal to the one of the insert operation and its path is longer" in {
    val path1 = AccessPath(0, 2, 1)
    val path2 = AccessPath(0, 2)
    val op1 = TreeDeleteOperation(path1, OperationId(), OperationContext(), ClientId())
    val op2 = TreeInsertOperation(path2, ValueTreeNode("abc"), OperationId(), OperationContext(), ClientId())

    val transformed = new TreeTransformer().transform((op1, op2))

    transformed should equal(TreeDeleteOperation(AccessPath(0, 3, 1), op1.id, OperationContext(List(op2.id)), op1.clientId))
  }

  it should "not change the delete operation when the index at the transformation point is equal to the one of the insert operation and its path is shorter" in {
    val path1 = AccessPath(0, 2)
    val path2 = AccessPath(0, 2, 1)
    val op1 = TreeDeleteOperation(path1, OperationId(), OperationContext(), ClientId())
    val op2 = TreeInsertOperation(path2, ValueTreeNode("abc"), OperationId(), OperationContext(), ClientId())


    val transformed = new TreeTransformer().transform((op1, op2))

    transformed should equal(TreeDeleteOperation(op1.accessPath, op1.id, OperationContext(List(op2.id)), op1.clientId))
  }

  it should "not bulk transform anything if the bridge is empty" in {
    val op = TreeDeleteOperation(AccessPath(), OperationId(), OperationContext(List.empty), ClientId("124"))

    val transformed = new TreeTransformer().bulkTransform(op, List.empty)

    transformed shouldBe empty
  }

  it should "bulk transform complete bridge and change context of first transformed operation" in {
    val op = TreeDeleteOperation(AccessPath(0, 0), OperationId(), OperationContext(List.empty), ClientId("124"))
    val op1 = TreeDeleteOperation(AccessPath(0, 1), OperationId(), OperationContext(List.empty), ClientId())
    val op2 = TreeDeleteOperation(AccessPath(0, 2), OperationId(), OperationContext(List(op1.id)), ClientId())

    val transformed = new TreeTransformer().bulkTransform(op, List(op2, op1))

    transformed should contain inOrder(
      TreeDeleteOperation(AccessPath(0, 1), op2.id, op2.operationContext, op2.clientId),
      TreeDeleteOperation(AccessPath(0, 0), op1.id, OperationContext(List(op.id)), op1.clientId))
  }

  it should "not change an insert when its transformed against a no-op" in {
    val op1 = TreeInsertOperation(AccessPath(0), ValueTreeNode("abc"), OperationId(), OperationContext(), ClientId())
    val op2 = TreeNoOperation(OperationId(), OperationContext(), ClientId())

    val transformed = new TreeTransformer().transform(op1, op2)

    transformed should equal(TreeInsertOperation(op1.accessPath, op1.tree, op1.id, OperationContext(List(op2.id)), op1.clientId))
  }

  it should "not change a delete when its transformed against a no-op" in {
    val op1 = TreeDeleteOperation(AccessPath(0, 2), OperationId(), OperationContext(), ClientId())
    val op2 = TreeNoOperation(OperationId(), OperationContext(), ClientId())

    val transformed = new TreeTransformer().transform(op1, op2)

    transformed should equal(TreeDeleteOperation(op1.accessPath, op1.id, OperationContext(List(op2.id)), op1.clientId))
  }

  it should "not change a no-op when its transformed against an operation" in {
    val op1 = TreeNoOperation(OperationId(), OperationContext(), ClientId())
    val op2 = TreeInsertOperation(AccessPath(0), ValueTreeNode("abc"), OperationId(), OperationContext(), ClientId())

    val transformed = new TreeTransformer().transform(op1, op2)

    transformed should equal(TreeNoOperation(op1.id, OperationContext(List(op2.id)), op1.clientId))
  }

  it should "increase the delete at the transformation point when it has the same path as an insert" in {
    val op1 = TreeDeleteOperation(AccessPath(0), OperationId(), OperationContext(), ClientId())
    val op2 = TreeInsertOperation(AccessPath(0), ValueTreeNode("abc"), OperationId(), OperationContext(), ClientId())

    val transformed = new TreeTransformer().transform(op1, op2)

    transformed should equal(TreeDeleteOperation(AccessPath(1), op1.id, OperationContext(List(op2.id)), op1.clientId))
  }
}
