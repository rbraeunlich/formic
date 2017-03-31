package de.tu_berlin.formic.datatype.tree

import de.tu_berlin.formic.common.datatype.{DataStructureName, DataStructureOperation, OperationContext}
import de.tu_berlin.formic.common.json.FormicJsonDataStructureProtocol
import de.tu_berlin.formic.common.{ClientId, OperationId}
import upickle.Js
import upickle.Js.Value
import upickle.default._

/**
  * @author Ronny Bräunlich
  */
case class TreeFormicJsonDataStructureProtocol[T](name: DataStructureName)(implicit val reader: Reader[T], val writer: Writer[T]) extends FormicJsonDataStructureProtocol {

  implicit val treeNodeWriter = new ValueTreeNodeWriter[T]

  implicit val treeNodeReader = new ValueTreeNodeReader[T]

  override def deserializeOperation(json: String): DataStructureOperation = {
    val valueMap = upickle.json.read(json).obj
    val operationId = OperationId(valueMap("operationId").str)
    val operationContext = valueMap("operationContext").arr.map(v => OperationId(v.str)).toList
    val clientId = ClientId(valueMap("clientId").str)
    val accessPath = AccessPath(valueMap("accessPath").arr.map(v => v.num.toInt):_*)
    if (valueMap.contains("tree")) {
      TreeInsertOperation(accessPath, readJs[ValueTreeNode](valueMap("tree")), operationId, OperationContext(operationContext), clientId)
    } else if (accessPath == AccessPath(-1)) {
      TreeNoOperation(operationId, OperationContext(operationContext), clientId)
    } else {
      TreeDeleteOperation(accessPath, operationId, OperationContext(operationContext), clientId)
    }
  }

  override def serializeOperation(op: DataStructureOperation): String = {
    op match {
      case ins: TreeInsertOperation =>
        write(
          Js.Obj(
            ("accessPath", writeJs(ins.accessPath.path)),
            ("tree", writeJs(ins.tree.asInstanceOf[ValueTreeNode])),
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
    }
  }
}

class ValueTreeNodeWriter[T]()(implicit val writer: Writer[T]) extends Writer[ValueTreeNode] {
  override def write0: (ValueTreeNode) => Value = {
    node =>
      Js.Obj(
        ("value", writeJs(node.value.asInstanceOf[T])),
        ("children", Js.Arr(node.children.map(this.write0): _*))
      )
  }
}

class ValueTreeNodeReader[T]()(implicit val reader: Reader[T]) extends Reader[ValueTreeNode] {

  override def read0: PartialFunction[Value, ValueTreeNode] = {
    case obj: Js.Obj =>
      val value = readJs[T](obj("value"))
      val children = obj("children").arr.map(this.read0)
      ValueTreeNode(value, children.toList)
  }
}
