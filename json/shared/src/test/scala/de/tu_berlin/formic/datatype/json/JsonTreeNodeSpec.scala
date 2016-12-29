package de.tu_berlin.formic.datatype.json

import de.tu_berlin.formic.common.datatype.OperationContext
import de.tu_berlin.formic.common.{ClientId, OperationId}
import de.tu_berlin.formic.datatype.tree.{AccessPath, TreeDeleteOperation, TreeInsertOperation}
import org.scalatest.{FlatSpec, Matchers}

/**
  * @author Ronny Bräunlich
  */
class JsonTreeNodeSpec extends FlatSpec with Matchers {

  "A boolean node" should "accept replace operation with boolean value" in {
    val node = BooleanNode("dull boy", value = true)
    val replacement = BooleanNode("dull boy", value = false)
    val operation = JsonReplaceOperation(AccessPath(), replacement, OperationId(), OperationContext(), ClientId())

    val after = node.applyOperation(operation)

    after should equal(replacement)
  }

  it should "reject replace operation with different type" in {
    val node = BooleanNode("dull boy", value = true)
    val replacement = NumberNode("dull boy", 5.0)
    val operation = JsonReplaceOperation(AccessPath(), replacement, OperationId(), OperationContext(), ClientId())

    an[IllegalArgumentException] should be thrownBy node.applyOperation(operation)
  }

  it should "reject a delete operation" in {
    val node = BooleanNode("dull boy", value = true)
    val operation = TreeDeleteOperation(AccessPath(), OperationId(), OperationContext(), ClientId())

    an[IllegalArgumentException] should be thrownBy node.applyOperation(operation)
  }

  it should "reject an insertion operation" in {
    val node = BooleanNode("dull boy", value = true)
    val operation = TreeInsertOperation(AccessPath(), BooleanNode("foo", value = false), OperationId(), OperationContext(), ClientId())

    an[IllegalArgumentException] should be thrownBy node.applyOperation(operation)
  }

  it should "reject an too long access path" in {
    val node = BooleanNode("dull boy", value = true)
    val replacement = BooleanNode("dull boy", value = false)
    val operation = JsonReplaceOperation(AccessPath(2), replacement, OperationId(), OperationContext(), ClientId())

    an[IllegalArgumentException] should be thrownBy node.applyOperation(operation)
  }

  "A number node" should "accept replace operation with boolean value" in {
    val node = NumberNode("balance", 2.0)
    val replacement = NumberNode("balance", 3.0)
    val operation = JsonReplaceOperation(AccessPath(), replacement, OperationId(), OperationContext(), ClientId())

    val after = node.applyOperation(operation)

    after should equal(replacement)
  }

  it should "reject replace operation with different type" in {
    val node = NumberNode("balance", 1.0)
    val replacement = BooleanNode("dull boy", value = true)
    val operation = JsonReplaceOperation(AccessPath(), replacement, OperationId(), OperationContext(), ClientId())

    an[IllegalArgumentException] should be thrownBy node.applyOperation(operation)
  }

  it should "reject a delete operation" in {
    val node = NumberNode("balance", 100.3)
    val operation = TreeDeleteOperation(AccessPath(), OperationId(), OperationContext(), ClientId())

    an[IllegalArgumentException] should be thrownBy node.applyOperation(operation)
  }

  it should "reject an insertion operation" in {
    val node = NumberNode("balance", 3.2)
    val operation = TreeInsertOperation(AccessPath(), NumberNode("foo", 5.0), OperationId(), OperationContext(), ClientId())

    an[IllegalArgumentException] should be thrownBy node.applyOperation(operation)
  }

  it should "reject an too long access path" in {
    val node = NumberNode("balance", 2.0)
    val replacement = NumberNode("balance", 3.0)
    val operation = JsonReplaceOperation(AccessPath(2), replacement, OperationId(), OperationContext(), ClientId())

    an[IllegalArgumentException] should be thrownBy node.applyOperation(operation)
  }

  "A character node" should "reject a replace operation" in {
    val node = CharacterNode("char", 'a')
    val operation = JsonReplaceOperation(AccessPath(), CharacterNode("char", 'b'), OperationId(), OperationContext(), ClientId())

    an[IllegalArgumentException] should be thrownBy node.applyOperation(operation)
  }

  it should "reject an insertion operation" in {
    val node = CharacterNode("char", 'a')
    val operation = TreeInsertOperation(AccessPath(), CharacterNode("char", 'b'), OperationId(), OperationContext(), ClientId())

    an[IllegalArgumentException] should be thrownBy node.applyOperation(operation)
  }

  it should "reject a delete operation" in {
    val node = CharacterNode("char", 'a')
    val operation = TreeDeleteOperation(AccessPath(), OperationId(), OperationContext(), ClientId())

    an[IllegalArgumentException] should be thrownBy node.applyOperation(operation)
  }

  "An array node" should "accept valid insertion" in {
    val children: List[JsonTreeNode[_]] = List(NumberNode("a", 1.0), BooleanNode("b", value = false))
    val node = ArrayNode("bar", children)
    val insertion = NumberNode("c", 3.0)
    val operation = TreeInsertOperation(AccessPath(2), insertion, OperationId(), OperationContext(), ClientId())

    val changed = node.applyOperation(operation)

    changed should equal(ArrayNode(node.key, children :+ insertion))
  }

  it should "accept valid deletion" in {
    val children: List[JsonTreeNode[_]] = List(NumberNode("a", 1.0), BooleanNode("b", value = false))
    val node = ArrayNode("bar", children)
    val insertion = NumberNode("c", 3.0)
    val operation = TreeDeleteOperation(AccessPath(0), OperationId(), OperationContext(), ClientId())

    val changed = node.applyOperation(operation)

    changed should equal(ArrayNode(node.key, List(BooleanNode("b", value = false))))
  }

  it should "accept a valid replace operation" in {
    val children: List[JsonTreeNode[_]] = List(NumberNode("a", 1.0), BooleanNode("b", value = false))
    val replacement = BooleanNode("f", value = true)
    val node = ArrayNode("bar", children)
    val insertion = NumberNode("c", 3.0)
    val operation = JsonReplaceOperation(AccessPath(1), replacement, OperationId(), OperationContext(), ClientId())

    val changed = node.applyOperation(operation)

    changed should equal(ArrayNode(node.key, List(NumberNode("a", 1.0), replacement)))
  }

  it should "forward insert operations" in {
    val children: List[JsonTreeNode[_]] = List(ArrayNode("q", List(NumberNode("a", 1.0))))
    val node = ArrayNode("bar", children)
    val insertion = NumberNode("c", 3.0)
    val operation = TreeInsertOperation(AccessPath(0, 1), insertion, OperationId(), OperationContext(), ClientId())

    val changed = node.applyOperation(operation)

    changed should equal(ArrayNode(node.key, List(ArrayNode("q", List(NumberNode("a", 1.0), insertion)))))
  }

  it should "forward delete operations" in {
    val children: List[JsonTreeNode[_]] = List(ArrayNode("q", List(NumberNode("a", 1.0), NumberNode("b", 2.0))))
    val node = ArrayNode("bar", children)
    val operation = TreeDeleteOperation(AccessPath(0, 1), OperationId(), OperationContext(), ClientId())

    val changed = node.applyOperation(operation)

    changed should equal(ArrayNode(node.key, List(ArrayNode("q", List(NumberNode("a", 1.0))))))
  }

  it should "forward replace operations" in {
    val children: List[JsonTreeNode[_]] = List(ArrayNode("q", List(NumberNode("a", 1.0))))
    val node = ArrayNode("bar", children)
    val replacement = NumberNode("c", 3.0)
    val operation = JsonReplaceOperation(AccessPath(0, 0), replacement, OperationId(), OperationContext(), ClientId())

    val changed = node.applyOperation(operation)

    changed should equal(ArrayNode(node.key, List(ArrayNode("q", List(replacement)))))
  }

  it should "reject an out of bounds index" in {
    val children: List[JsonTreeNode[_]] = List(NumberNode("a", 1.0), BooleanNode("b", value = false))
    val node = ArrayNode("bar", children)
    val insertion = NumberNode("c", 3.0)
    val operation = TreeInsertOperation(AccessPath(10), insertion, OperationId(), OperationContext(), ClientId())

    an[ArrayIndexOutOfBoundsException] should be thrownBy node.applyOperation(operation)
  }

  it should "reject invalid deletion" in {
    val node = ArrayNode("bar", List.empty)
    val operation = TreeDeleteOperation(AccessPath(0),  OperationId(), OperationContext(), ClientId())

    an[ArrayIndexOutOfBoundsException] should be thrownBy node.applyOperation(operation)
  }

  it should "reject invalid replacement" in {
    val node = ArrayNode("bar", List.empty)
    val operation = JsonReplaceOperation(AccessPath(0), NumberNode("0", 5.0), OperationId(), OperationContext(), ClientId())

    an[ArrayIndexOutOfBoundsException] should be thrownBy node.applyOperation(operation)
  }

  "A string node" should "accept insertion of character node" in {
    val node = StringNode("text", List.empty)
    val insertion = CharacterNode("0", 'c')
    val operation = TreeInsertOperation(AccessPath(0), insertion, OperationId(), OperationContext(), ClientId())

    val changed = node.applyOperation(operation)

    changed should equal(StringNode(node.key, List(insertion)))
  }

  it should "reject too long access path" in {
    val node = StringNode("text", List.empty)
    val insertion = CharacterNode(null, 'c')
    val operation = TreeInsertOperation(AccessPath(0, 2), insertion, OperationId(), OperationContext(), ClientId())

    an[IllegalArgumentException] should be thrownBy node.applyOperation(operation)
  }

  it should "reject an out of bounds index" in {
    val node = StringNode("text", List.empty)
    val insertion = CharacterNode("0", 'c')
    val operation = TreeInsertOperation(AccessPath(11), insertion, OperationId(), OperationContext(), ClientId())

    an[ArrayIndexOutOfBoundsException] should be thrownBy node.applyOperation(operation)
  }

  it should "accept valid deletion" in {
    val node = StringNode("text", List(CharacterNode("0", 'f')))
    val operation = TreeDeleteOperation(AccessPath(0),  OperationId(), OperationContext(), ClientId())

    val changed = node.applyOperation(operation)

    changed should equal(StringNode(node.key, List.empty))
  }

  it should "reject invalid deletion" in {
    val node = StringNode("text", List.empty)
    val operation = TreeDeleteOperation(AccessPath(0),  OperationId(), OperationContext(), ClientId())

    an[ArrayIndexOutOfBoundsException] should be thrownBy node.applyOperation(operation)
  }

  it should "reject invalid replacement" in {
    val node = StringNode("text", List.empty)
    val operation = JsonReplaceOperation(AccessPath(0), CharacterNode("0", 'a'), OperationId(), OperationContext(), ClientId())

    an[ArrayIndexOutOfBoundsException] should be thrownBy node.applyOperation(operation)
  }

  it should "accept valid replacement" in {
    val node = StringNode("text", List(CharacterNode("0", 'a'), CharacterNode("1", 'c')))
    val insertion = CharacterNode("1", 'b')
    val operation = JsonReplaceOperation(AccessPath(1), insertion, OperationId(), OperationContext(), ClientId())

    val changed = node.applyOperation(operation)

    changed should equal(StringNode(node.key, List(CharacterNode("0", 'a'), insertion)))
  }

  "An object node" should "accept a valid insertion" in {

  }

  it should "accept a valid deletion" in {

  }

  it should "accept a valid replacement" in {

  }

  it should "reject an invalid insertion" in {

  }

  it should "reject an invalid deletion" in {

  }

  it should "reject an invalid replacement" in {

  }

  it should "forward insertion operations" in {

  }

  it should "forward delete operations" in {

  }

  it should "forward replace operations" in {

  }
}
