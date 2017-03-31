package de.tu_berlin.formic.datastructure.json

import de.tu_berlin.formic.common.datastructure.OperationContext
import de.tu_berlin.formic.common.{ClientId, OperationId}
import de.tu_berlin.formic.datatype.tree.{AccessPath, TreeDeleteOperation, TreeInsertOperation, TreeNoOperation}
import org.scalatest.{FlatSpec, Matchers}

/**
  * @author Ronny Bräunlich
  */
class JsonTransformerSpec extends FlatSpec with Matchers {

  "A JSON transformer" should "not change a replace operation when it is effect independent with an insertion" in {
    val path1 = AccessPath(3, 4, 5)
    val path2 = AccessPath(0, 1)
    val replace = JsonReplaceOperation(path1, NumberNode("abc", 1.0), OperationId(), OperationContext(), ClientId())
    val insert = TreeInsertOperation(path2, CharacterNode("def", 'c'), OperationId(), OperationContext(), ClientId())

    val transformed = new JsonTransformer().transform((replace, insert))

    transformed should equal(JsonReplaceOperation(replace.accessPath, replace.tree, replace.id, OperationContext(List(insert.id)), replace.clientId))
  }

  it should "not change a replace operation when its index at the transformation point is lower than the one of an insertion" in {
    val path1 = AccessPath(0, 0, 5)
    val path2 = AccessPath(0, 1)
    val replace = JsonReplaceOperation(path1, NumberNode("abc", 1.0), OperationId(), OperationContext(), ClientId())
    val insert = TreeInsertOperation(path2, CharacterNode("def", 'c'), OperationId(), OperationContext(), ClientId())

    val transformed = new JsonTransformer().transform((replace, insert))

    transformed should equal(JsonReplaceOperation(replace.accessPath, replace.tree, replace.id, OperationContext(List(insert.id)), replace.clientId))
  }

  it should "increase the index of a replace operation when its index at the transformation point is higher than the one of an insertion" in {
    val path1 = AccessPath(0, 4)
    val path2 = AccessPath(0, 1)
    val replace = JsonReplaceOperation(path1, NumberNode("abc", 1.0), OperationId(), OperationContext(), ClientId())
    val insert = TreeInsertOperation(path2, CharacterNode("def", 'c'), OperationId(), OperationContext(), ClientId())

    val transformed = new JsonTransformer().transform((replace, insert))

    transformed should equal(JsonReplaceOperation(AccessPath(0, 5), replace.tree, replace.id, OperationContext(List(insert.id)), replace.clientId))
  }

  it should "increase the index of a replace operation when its access path is longer than the one of an insertion" in {
    val path1 = AccessPath(0, 1, 5)
    val path2 = AccessPath(0, 1)
    val replace = JsonReplaceOperation(path1, NumberNode("abc", 1.0), OperationId(), OperationContext(), ClientId())
    val insert = TreeInsertOperation(path2, CharacterNode("def", 'c'), OperationId(), OperationContext(), ClientId())

    val transformed = new JsonTransformer().transform((replace, insert))

    transformed should equal(JsonReplaceOperation(AccessPath(0, 2, 5), replace.tree, replace.id, OperationContext(List(insert.id)), replace.clientId))
  }

  it should "not change the replace operation when its access path is shorter than the one of an insertion" in {
    val path1 = AccessPath(3, 4, 5)
    val path2 = AccessPath(3, 4, 5, 6)
    val replace = JsonReplaceOperation(path1, NumberNode("abc", 1.0), OperationId(), OperationContext(), ClientId())
    val insert = TreeInsertOperation(path2, CharacterNode("def", 'c'), OperationId(), OperationContext(), ClientId())

    val transformed = new JsonTransformer().transform((replace, insert))

    transformed should equal(JsonReplaceOperation(replace.accessPath, replace.tree, replace.id, OperationContext(List(insert.id)), replace.clientId))
  }

  it should "not change a replace operation when it is effect independent with a deletion" in {
    val path1 = AccessPath(3,4,5)
    val path2 = AccessPath(0,1)
    val replace = JsonReplaceOperation(path1, NumberNode("abc", 1.0), OperationId(), OperationContext(), ClientId())
    val insert = TreeDeleteOperation(path2, OperationId(), OperationContext(), ClientId())

    val transformed = new JsonTransformer().transform((replace, insert))

    transformed should equal(JsonReplaceOperation(replace.accessPath, replace.tree, replace.id, OperationContext(List(insert.id)), replace.clientId))
  }

  it should "not change a replace operation when its index at the transformation point is lower than the one of a deletion" in {
    val path1 = AccessPath(3,3,5)
    val path2 = AccessPath(3,4)
    val replace = JsonReplaceOperation(path1, NumberNode("abc", 1.0), OperationId(), OperationContext(), ClientId())
    val insert = TreeDeleteOperation(path2, OperationId(), OperationContext(), ClientId())

    val transformed = new JsonTransformer().transform((replace, insert))

    transformed should equal(JsonReplaceOperation(replace.accessPath, replace.tree, replace.id, OperationContext(List(insert.id)), replace.clientId))
  }

  it should "decrease the index of a replace operation when its index at the transformation point is higher than the one of a deletion" in {
    val path1 = AccessPath(0,2)
    val path2 = AccessPath(0,1)
    val replace = JsonReplaceOperation(path1, NumberNode("abc", 1.0), OperationId(), OperationContext(), ClientId())
    val insert = TreeDeleteOperation(path2, OperationId(), OperationContext(), ClientId())

    val transformed = new JsonTransformer().transform((replace, insert))

    transformed should equal(JsonReplaceOperation(AccessPath(0,1), replace.tree, replace.id, OperationContext(List(insert.id)), replace.clientId))
  }

  it should "decrease the index of a replace operation when its access path is longer than the one of a deletion" in {
    val path1 = AccessPath(0,1,5)
    val path2 = AccessPath(0,1)
    val replace = JsonReplaceOperation(path1, NumberNode("abc", 1.0), OperationId(), OperationContext(), ClientId())
    val insert = TreeDeleteOperation(path2, OperationId(), OperationContext(), ClientId())

    val transformed = new JsonTransformer().transform((replace, insert))

    transformed should equal(JsonReplaceOperation(AccessPath(0,0,5), replace.tree, replace.id, OperationContext(List(insert.id)), replace.clientId))
  }

  it should "not change the replace operation when its access path is shorter than the one of a deletion" in {
    val path1 = AccessPath(0)
    val path2 = AccessPath(0,1)
    val replace = JsonReplaceOperation(path1, NumberNode("abc", 1.0), OperationId(), OperationContext(), ClientId())
    val insert = TreeDeleteOperation(path2, OperationId(), OperationContext(), ClientId())

    val transformed = new JsonTransformer().transform((replace, insert))

    transformed should equal(JsonReplaceOperation(replace.accessPath, replace.tree, replace.id, OperationContext(List(insert.id)), replace.clientId))
  }

  it should "not change the first replace operation when its effect independent with other replace operation" in {
    val path1 = AccessPath(3, 4, 5)
    val path2 = AccessPath(0, 1)
    val replace = JsonReplaceOperation(path1, NumberNode("abc", 1.0), OperationId(), OperationContext(), ClientId())
    val replace2 = JsonReplaceOperation(path2, NumberNode("def", 2.0), OperationId(), OperationContext(), ClientId())

    val transformed = new JsonTransformer().transform((replace, replace2))

    transformed should equal(JsonReplaceOperation(replace.accessPath, replace.tree, replace.id, OperationContext(List(replace2.id)), replace.clientId))
  }

  it should "return a no operation when both replace operations are equal" in {
    val path1 = AccessPath(3, 4, 5)
    val replace = JsonReplaceOperation(path1, NumberNode("abc", 1.0), OperationId(), OperationContext(), ClientId())

    val transformed = new JsonTransformer().transform((replace, replace))

    transformed should equal(TreeNoOperation(replace.id, OperationContext(List(replace.id)), replace.clientId))
  }

  it should "return a no operation when both replace operations have the same path and the client id of the first replace is smaller than the second one" in {
    val path1 = AccessPath(0)
    val path2 = AccessPath(0)
    val replace = JsonReplaceOperation(path1, NumberNode("abc", 1.0), OperationId(), OperationContext(), ClientId("1"))
    val replace2 = JsonReplaceOperation(path2, NumberNode("def", 2.0), OperationId(), OperationContext(), ClientId("2"))

    val transformed = new JsonTransformer().transform((replace, replace2))

    transformed should equal(TreeNoOperation(replace.id, OperationContext(List(replace2.id)), replace.clientId))
  }

  it should "not change the first replace operation when  both replace operations have the same path and the client id of the first one is larger" in {
    val path1 = AccessPath(3, 4, 5)
    val path2 = AccessPath(0, 1)
    val replace = JsonReplaceOperation(path1, NumberNode("abc", 1.0), OperationId(), OperationContext(), ClientId("2"))
    val replace2 = JsonReplaceOperation(path2, NumberNode("def", 2.0), OperationId(), OperationContext(), ClientId("1"))

    val transformed = new JsonTransformer().transform((replace, replace2))

    transformed should equal(JsonReplaceOperation(replace.accessPath, replace.tree, replace.id, OperationContext(List(replace2.id)), replace.clientId))
  }

  it should "not change the insertion when its transformed against a replace" in {
    val path1 = AccessPath(3)
    val path2 = AccessPath(3)
    val replace = JsonReplaceOperation(path1, NumberNode("abc", 1.0), OperationId(), OperationContext(), ClientId())
    val insert = TreeInsertOperation(path2, CharacterNode("def", 'c'), OperationId(), OperationContext(), ClientId())

    val transformed = new JsonTransformer().transform((insert, replace))

    transformed should equal(TreeInsertOperation(insert.accessPath, insert.tree, insert.id, OperationContext(List(replace.id)), insert.clientId))
  }

  it should "not change the deletion when its transformed against a replace" in {
    val path1 = AccessPath(3)
    val path2 = AccessPath(3)
    val replace = JsonReplaceOperation(path1, NumberNode("abc", 1.0), OperationId(), OperationContext(), ClientId())
    val delete = TreeDeleteOperation(path2, OperationId(), OperationContext(), ClientId())

    val transformed = new JsonTransformer().transform((delete, replace))

    transformed should equal(TreeDeleteOperation(delete.accessPath, delete.id, OperationContext(List(replace.id)), delete.clientId))
  }
}
