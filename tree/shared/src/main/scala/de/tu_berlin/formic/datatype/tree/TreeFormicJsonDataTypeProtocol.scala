package de.tu_berlin.formic.datatype.tree

import de.tu_berlin.formic.common.datatype.{DataTypeName, DataTypeOperation, OperationContext}
import de.tu_berlin.formic.common.json.FormicJsonDataTypeProtocol
import de.tu_berlin.formic.common.{ClientId, OperationId}
import upickle.Js
import upickle.Js.Value
import upickle.default._

import scala.collection.mutable.ArrayBuffer

/**
  * @author Ronny Bräunlich
  */
class TreeFormicJsonDataTypeProtocol[T](val name: DataTypeName)(implicit val reader: Reader[T], val writer: Writer[T]) extends FormicJsonDataTypeProtocol {

  implicit val treeNodeWriter = new TreeNodeWriter[T]

  implicit val treeNodeReader = new TreeNodeReader[T]

  override def deserializeOperation(json: String): DataTypeOperation = {
    val valueMap = upickle.json.read(json).obj
    val operationId = OperationId(valueMap("operationId").str)
    val operationContext = valueMap("operationContext").arr.map(v => OperationId(v.str)).toList
    val clientId = ClientId(valueMap("clientId").str)
    val accessPath = AccessPath(valueMap("accessPath").arr.map(v => v.num.toInt).toList)
    if (valueMap.contains("tree")) {
      TreeInsertOperation(accessPath, readJs[TreeNode](valueMap("tree")), operationId, OperationContext(operationContext), clientId)
    } else {
      TreeDeleteOperation(accessPath, operationId, OperationContext(operationContext), clientId)
    }
  }

  override def serializeOperation(op: DataTypeOperation): String = {
    op match {
      case ins: TreeInsertOperation =>
        write(
          Js.Obj(
            ("accessPath", writeJs(ins.accessPath.list)),
            ("tree", writeJs(ins.tree)),
            ("operationId", Js.Str(op.id.id)),
            ("operationContext", Js.Arr(op.operationContext.operations.map(o => Js.Str(o.id)): _*)),
            ("clientId", Js.Str(op.clientId.id))
          )
        )
      case del: TreeDeleteOperation =>
        write(
          Js.Obj(
            ("accessPath", writeJs(del.accessPath.list)),
            ("operationId", Js.Str(op.id.id)),
            ("operationContext", Js.Arr(op.operationContext.operations.map(o => Js.Str(o.id)): _*)),
            ("clientId", Js.Str(op.clientId.id))
          )
        )
    }
  }
}

class TreeNodeWriter[T]()(implicit val writer: Writer[T]) extends Writer[TreeNode] {
  override def write0: (TreeNode) => Value = {
    node =>
      Js.Obj(
        ("value", writeJs(node.value.asInstanceOf[T])),
        ("children", Js.Arr(node.children.map(this.write0): _*))
      )
  }
}

class TreeNodeReader[T]()(implicit val reader: Reader[T]) extends Reader[TreeNode] {

  override def read0: PartialFunction[Value, TreeNode] = {
    case obj: Js.Obj =>
      val value = readJs[T](obj("value"))
      val children = obj("children").arr.map(this.read0)
      TreeNode(value, children.to[ArrayBuffer])
  }

}