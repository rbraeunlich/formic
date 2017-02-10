package de.tu_berlin.formic.datatype.json

import de.tu_berlin.formic.common.datatype.OperationContext
import de.tu_berlin.formic.common.{ClientId, OperationId}
import de.tu_berlin.formic.datatype.tree.{AccessPath, TreeDeleteOperation, TreeInsertOperation}
import org.scalatest.{FlatSpec, Matchers}

/**
  * @author Ronny Bräunlich
  */
class JsonTreeNodeSpec extends FlatSpec with Matchers {

  "A boolean node" should "reject replace operation with boolean value" in {
    val node = BooleanNode("dull boy", value = true)
    val replacement = BooleanNode("dull boy", value = false)
    val operation = JsonReplaceOperation(AccessPath(), replacement, OperationId(), OperationContext(), ClientId())

    an[IllegalArgumentException] should be thrownBy node.applyOperation(operation)
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

  "A number node" should "reject replace operation with number value" in {
    val node = NumberNode("balance", 2.0)
    val replacement = NumberNode("balance", 3.0)
    val operation = JsonReplaceOperation(AccessPath(), replacement, OperationId(), OperationContext(), ClientId())

    an[IllegalArgumentException] should be thrownBy node.applyOperation(operation)
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
    val replacement = CharacterNode("char", 'b')
    val operation = JsonReplaceOperation(AccessPath(), replacement, OperationId(), OperationContext(), ClientId())

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
    val children: List[JsonTreeNode[_]] = List(NumberNode(null, 1.0), BooleanNode(null, value = false))
    val node = ArrayNode("bar", children)
    val insertion = NumberNode(null, 3.0)
    val operation = TreeInsertOperation(AccessPath(2), insertion, OperationId(), OperationContext(), ClientId())

    val changed = node.applyOperation(operation)

    changed should equal(ArrayNode(node.key, children :+ insertion))
  }

  it should "accept valid deletion" in {
    val children: List[JsonTreeNode[_]] = List(NumberNode(null, 1.0), BooleanNode(null, value = false))
    val node = ArrayNode("bar", children)
    val operation = TreeDeleteOperation(AccessPath(0), OperationId(), OperationContext(), ClientId())

    val changed = node.applyOperation(operation)

    changed should equal(ArrayNode(node.key, List(BooleanNode(null, value = false))))
  }

  it should "accept a valid replace operation" in {
    val children: List[JsonTreeNode[_]] = List(NumberNode(null, 1.0), BooleanNode(null, value = false))
    val replacement = BooleanNode(null, value = true)
    val node = ArrayNode("bar", children)
    val operation = JsonReplaceOperation(AccessPath(1), replacement, OperationId(), OperationContext(), ClientId())

    val changed = node.applyOperation(operation)

    changed should equal(ArrayNode(node.key, List(NumberNode(null, 1.0), replacement)))
  }

  it should "forward insert operations" in {
    val children: List[JsonTreeNode[_]] = List(ArrayNode("q", List(NumberNode(null, 1.0))))
    val node = ArrayNode("bar", children)
    val insertion = NumberNode(null, 3.0)
    val operation = TreeInsertOperation(AccessPath(0, 1), insertion, OperationId(), OperationContext(), ClientId())

    val changed = node.applyOperation(operation)

    changed should equal(ArrayNode(node.key, List(ArrayNode("q", List(NumberNode(null, 1.0), insertion)))))
  }

  it should "forward delete operations" in {
    val children: List[JsonTreeNode[_]] = List(ArrayNode("q", List(NumberNode(null, 1.0), NumberNode(null, 2.0))))
    val node = ArrayNode("bar", children)
    val operation = TreeDeleteOperation(AccessPath(0, 1), OperationId(), OperationContext(), ClientId())

    val changed = node.applyOperation(operation)

    changed should equal(ArrayNode(node.key, List(ArrayNode("q", List(NumberNode(null, 1.0))))))
  }

  it should "forward replace operations" in {
    val children: List[JsonTreeNode[_]] = List(ArrayNode("q", List(NumberNode(null, 1.0))))
    val node = ArrayNode("bar", children)
    val replacement = NumberNode(null, 3.0)
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
    val operation = TreeDeleteOperation(AccessPath(0), OperationId(), OperationContext(), ClientId())

    an[ArrayIndexOutOfBoundsException] should be thrownBy node.applyOperation(operation)
  }

  it should "reject invalid replacement" in {
    val node = ArrayNode("bar", List.empty)
    val operation = JsonReplaceOperation(AccessPath(0), NumberNode("0", 5.0), OperationId(), OperationContext(), ClientId())

    an[ArrayIndexOutOfBoundsException] should be thrownBy node.applyOperation(operation)
  }

  it should "replace an object node within itself" in {
    val node = ArrayNode("bar", List(ObjectNode("toReplace", List(NumberNode("num", 1)))))
    val replacement = ObjectNode("toReplace", List(NumberNode("num", 2)))
    val operation = JsonReplaceOperation(AccessPath(0), replacement, OperationId(), OperationContext(), ClientId())

    node.applyOperation(operation) should equal(ArrayNode(node.key, List(replacement)))
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
    val node = StringNode("text", List(CharacterNode(null, 'f'), CharacterNode(null, 'g')))
    val operation = TreeDeleteOperation(AccessPath(0), OperationId(), OperationContext(), ClientId())

    val changed = node.applyOperation(operation)

    changed should equal(StringNode(node.key, List(CharacterNode(null, 'g'))))
  }

  it should "reject invalid deletion" in {
    val node = StringNode("text", List.empty)
    val operation = TreeDeleteOperation(AccessPath(0), OperationId(), OperationContext(), ClientId())

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
    val node = ObjectNode(null, List.empty)
    val insertion = NumberNode("foo", 1.0)
    val operation = TreeInsertOperation(AccessPath(0), insertion, OperationId(), OperationContext(), ClientId())

    val changed = node.applyOperation(operation)

    changed should equal(ObjectNode(null, List(insertion)))
  }

  it should "accept a valid deletion" in {
    val node = ObjectNode(null, List(NumberNode("foo", 1.0)))
    val operation = TreeDeleteOperation(AccessPath(0), OperationId(), OperationContext(), ClientId())

    val changed = node.applyOperation(operation)

    changed should equal(ObjectNode(null, List.empty))
  }

  it should "accept a valid replacement" in {
    val node = ObjectNode(null, List(NumberNode("foo", 1.0)))
    val replacement = NumberNode("foo", 2.0)
    val operation = JsonReplaceOperation(AccessPath(0), replacement, OperationId(), OperationContext(), ClientId())

    val changed = node.applyOperation(operation)

    changed should equal(ObjectNode(null, List(replacement)))
  }

  it should "reject an invalid insertion path" in {
    val node = ObjectNode(null, List.empty)
    val insertion = NumberNode("foo", 1.0)
    val operation = TreeInsertOperation(AccessPath(5), insertion, OperationId(), OperationContext(), ClientId())

    an[ArrayIndexOutOfBoundsException] should be thrownBy node.applyOperation(operation)
  }

  it should "reject an invalid deletion" in {
    val node = ObjectNode(null, List.empty)
    val operation = TreeDeleteOperation(AccessPath(5), OperationId(), OperationContext(), ClientId())

    an[ArrayIndexOutOfBoundsException] should be thrownBy node.applyOperation(operation)
  }

  it should "reject an invalid replacement" in {
    val node = ObjectNode(null, List.empty)
    val operation = JsonReplaceOperation(AccessPath(5), NumberNode("foo", 1.0), OperationId(), OperationContext(), ClientId())

    an[ArrayIndexOutOfBoundsException] should be thrownBy node.applyOperation(operation)
  }

  it should "forward insertion operations" in {
    val nested = ObjectNode("nested", List.empty)
    val node = ObjectNode(null, List(nested))
    val insertion = NumberNode("foo", 1.0)
    val operation = TreeInsertOperation(AccessPath(0, 0), insertion, OperationId(), OperationContext(), ClientId())

    val changed = node.applyOperation(operation)

    changed should equal(ObjectNode(null, List(ObjectNode(nested.key, List(insertion)))))
  }

  it should "forward delete operations" in {
    val nested = ObjectNode("nested", List(NumberNode("num", 2.0)))
    val node = ObjectNode(null, List(nested))
    val operation = TreeDeleteOperation(AccessPath(0, 0), OperationId(), OperationContext(), ClientId())

    val changed = node.applyOperation(operation)

    changed should equal(ObjectNode(null, List(ObjectNode(nested.key, List.empty))))
  }

  it should "forward replace operations" in {
    val nested = ObjectNode("nested", List(NumberNode("num", 2.0)))
    val node = ObjectNode(null, List(nested))
    val replacement = NumberNode("num", 1.0)
    val operation = JsonReplaceOperation(AccessPath(0, 0), replacement, OperationId(), OperationContext(), ClientId())

    val changed = node.applyOperation(operation)

    changed should equal(ObjectNode(null, List(ObjectNode(nested.key, List(replacement)))))
  }

  it should "reject duplicated keys" in {
    val node = ObjectNode(null, List(NumberNode("foo", 2.0)))
    val insertion = NumberNode("foo", 1.0)
    val operation = TreeInsertOperation(AccessPath(0), insertion, OperationId(), OperationContext(), ClientId())

    an[IllegalArgumentException] should be thrownBy node.applyOperation(operation)
  }

  it should "sort its children by key" in {
    val lastNode = NumberNode("z", 2.0)
    val middleNode = NumberNode("q", 100.3)
    val firstNode = NumberNode("a", 3000)
    val node = ObjectNode(null, List(middleNode,lastNode, firstNode))

    node.getData should contain inOrder(firstNode, middleNode, lastNode)
  }

  it should "correctly translate a JSON Path for insertion pointing below root" in {
    val node = ObjectNode(null, List.empty)

    node.translateJsonPathForInsertion(JsonPath("foo")) should equal(AccessPath(0))
  }

  it should "correctly translate a JSON Path for insertion pointing below an Array" in {
    val node = ObjectNode(null, List(ArrayNode("arr", List.empty)))

    node.translateJsonPathForInsertion(JsonPath("arr", "0")) should equal(AccessPath(0,0))
  }

  it should "correctly translate a JSON Path for insertion pointing below an object" in {
    val node = ObjectNode(null, List(ObjectNode("obj", List(NumberNode("num", 1)))))

    node.translateJsonPathForInsertion(JsonPath("obj", "opq")) should equal(AccessPath(0,1))
  }

  it should "correctly translate a JSON path" in {
    val secondLevel = List(NumberNode("21", 2.0), NumberNode("22", 1.0))
    val firstLevel = List(NumberNode("11", 100), ObjectNode("12", secondLevel))
    val node = ObjectNode(null, firstLevel)

    node.translateJsonPath(JsonPath("12", "22")) should equal(AccessPath(1, 1))
  }

  it should "correctly translate a JSON path with an array in between" in {
    val secondLevel = List(ArrayNode("21", List(NumberNode(null, 2.0), NumberNode(null, 1.0))))
    val firstLevel = List(NumberNode("11", 100), ObjectNode("12", secondLevel))
    val node = ObjectNode(null, firstLevel)

    node.translateJsonPath(JsonPath("12", "21", "0")) should equal(AccessPath(1, 0, 0))
  }

  it should "replace and array within itself" in {
    val node = ObjectNode(null, List(ArrayNode("foo", List.empty)))
    val replacement = ArrayNode("foo", List(NumberNode(null, 5)))
    val operation = JsonReplaceOperation(AccessPath(0), replacement, OperationId(), OperationContext(), ClientId())

    val changed = node.applyOperation(operation)

    changed should equal(ObjectNode(null, List(replacement)))
  }

  it should "translate paths exactly" in {
    //only because "ships" is contained in "shipsSunk" it should not match
    val tree = ObjectNode(null, List(NumberNode("boardSize",7), NumberNode("numShips",3), NumberNode("shipLength",3), NumberNode("shipsSunk",0)))

    an[IllegalArgumentException] should be thrownBy tree.translateJsonPath(JsonPath("ships"))
  }

  it should "have a hash value when it's empty" in {
    val node = ObjectNode(null, List.empty)

    noException should be thrownBy node.hashCode()
  }
}
