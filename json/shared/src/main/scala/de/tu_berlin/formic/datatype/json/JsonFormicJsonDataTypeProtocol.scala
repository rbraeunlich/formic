package de.tu_berlin.formic.datatype.json

import de.tu_berlin.formic.common.datatype.{DataTypeName, DataTypeOperation, OperationContext}
import de.tu_berlin.formic.common.json.FormicJsonDataTypeProtocol
import de.tu_berlin.formic.common.{ClientId, OperationId}
import de.tu_berlin.formic.datatype.tree._
import upickle.Js
import upickle.Js.Value
import upickle.default._

/**
  * The serialization is a little bit tricky, because upickle would treat every type of node as
  * a single object, but we want to end up with a valid JSON representation.
  *
  * @author Ronny Bräunlich
  */
class JsonFormicJsonDataTypeProtocol(val name: DataTypeName)(implicit val reader: Reader[ObjectNode], val writer: Writer[ObjectNode]) extends FormicJsonDataTypeProtocol {

  override def deserializeOperation(json: String): DataTypeOperation = {
    val valueMap = upickle.json.read(json).obj
    val operationId = OperationId(valueMap("operationId").str)
    val operationContext = valueMap("operationContext").arr.map(v => OperationId(v.str)).toList
    val clientId = ClientId(valueMap("clientId").str)
    val accessPath = AccessPath(valueMap("accessPath").arr.map(v => v.num.toInt): _*)
    if (valueMap.contains("type")) {
      JsonReplaceOperation(accessPath, unwrapTreeIfItIsWrapped(readJs[ObjectNode](valueMap("object"))), operationId, OperationContext(operationContext), clientId)
    } else if (valueMap.contains("object")) {
      //here we unwrap the hack again
      TreeInsertOperation(accessPath, unwrapTreeIfItIsWrapped(readJs[ObjectNode](valueMap("object"))), operationId, OperationContext(operationContext), clientId)
    } else if (accessPath == AccessPath(-1)) {
      TreeNoOperation(operationId, OperationContext(operationContext), clientId)
    } else {
      TreeDeleteOperation(accessPath, operationId, OperationContext(operationContext), clientId)
    }
  }

  override def serializeOperation(op: DataTypeOperation): String = {
    op match {
      case ins: TreeInsertOperation =>
        write(
          Js.Obj(
            ("accessPath", writeJs(ins.accessPath.path)),
            ("object", writeJs(wrapTreeIfItIsNotAnObjectNode(ins.tree))),
            ("operationId", Js.Str(op.id.id)),
            ("operationContext", Js.Arr(op.operationContext.operations.map(o => Js.Str(o.id)): _*)),
            ("clientId", Js.Str(op.clientId.id))
          )
        )
      case del: TreeDeleteOperation =>
        write(
          Js.Obj(
            ("accessPath", writeJs(del.accessPath.path)),
            ("operationId", Js.Str(op.id.id)),
            ("operationContext", Js.Arr(op.operationContext.operations.map(o => Js.Str(o.id)): _*)),
            ("clientId", Js.Str(op.clientId.id))
          )
        )
      case no: TreeNoOperation =>
        write(
          Js.Obj(
            ("accessPath", writeJs(no.accessPath.path)),
            ("operationId", Js.Str(op.id.id)),
            ("operationContext", Js.Arr(op.operationContext.operations.map(o => Js.Str(o.id)): _*)),
            ("clientId", Js.Str(op.clientId.id))
          )
        )
      case repl: JsonReplaceOperation =>
        write(
          Js.Obj(
            //to distinguish replace from inserts
            ("type", writeJs(Js.Str("replace"))),
            ("accessPath", writeJs(repl.accessPath.path)),
            ("object", writeJs(wrapTreeIfItIsNotAnObjectNode(repl.tree))),
            ("operationId", Js.Str(op.id.id)),
            ("operationContext", Js.Arr(op.operationContext.operations.map(o => Js.Str(o.id)): _*)),
            ("clientId", Js.Str(op.clientId.id))
          )
        )
    }
  }

  /**
    * This is a little hack so we don't have to distinguish every single node type within the reader and writer.
    * Every tree for an insert or replace operation that is not an ObjectNode itself is wrapped in one
    * and unwrapped later again.
    *
    * @param tree the three to wrap
    * @return the original tree wrapper inside an ObjectNode
    */
  def wrapTreeIfItIsNotAnObjectNode(tree: TreeNode): ObjectNode = {
    tree match {
      case obj: ObjectNode => obj
      case rest: TreeNode => ObjectNode(null, List(rest.asInstanceOf[JsonTreeNode[_]]))
    }
  }

  /**
    * Here we unwrap the hack again
    */
  def unwrapTreeIfItIsWrapped(tree: ObjectNode): JsonTreeNode[_] = {
    //only the root node of a JsonTree has a key of null else it must be the wrapper
    if (tree.children.length == 1 && tree.key == null) {
      tree.children.head
    } else {
      tree
    }
  }
}

object JsonFormicJsonDataTypeProtocol {
  implicit val writer = new JsonTreeNodeWriter
  implicit val reader = new JsonTreeNodeReader
}

/**
  * Every Json Data type must have a ObjectNode as root, therefore we only need a single writer for that class.
  */
class JsonTreeNodeWriter extends Writer[ObjectNode] {

  override def write0: (ObjectNode) => Value = {
    node =>
      Js.Obj(node.children.map(collectKeyValueForChild): _*)
  }

  def collectKeyValueForChild(value: JsonTreeNode[_]): (String, Js.Value) = {
    value match {
      case NumberNode(k, v) => k -> Js.Num(v)
      case BooleanNode(k, v) => k -> (if (v) Js.True else Js.False)
      case stringNode: StringNode => stringNode.key -> Js.Str(stringNode.getData)
      case ArrayNode(k, values) => k -> Js.Arr(values.map(collectKeyValueForChild).map(t => t._2): _*)
      case ObjectNode(k, values) => k -> Js.Obj(values.map(collectKeyValueForChild): _*)
    }
  }
}

class JsonTreeNodeReader extends Reader[ObjectNode] {

  override def read0: PartialFunction[Value, ObjectNode] = {
    case obj: Js.Obj =>
      ObjectNode(null, obj.value.map(transformValuesToNodes).toList)
  }

  def transformValuesToNodes(tuple: (String, Value)): JsonTreeNode[_] = {
    tuple match {
      case (key, Js.Num(num)) => NumberNode(key, num)
      case (key, Js.True) => BooleanNode(key, value = true)
      case (key, Js.False) => BooleanNode(key, value = false)
      case (key, Js.Str(str)) => StringNode(key, str.toCharArray.map(char => CharacterNode(null, char)).toList)
      case (key, arr: Js.Arr) => ArrayNode(key, arr.arr.map(value => (null, value)).map(transformValuesToNodes).toList)
      case (key, obj: Js.Obj) => ObjectNode(key, obj.value.map(transformValuesToNodes).toList)
    }
  }
}
