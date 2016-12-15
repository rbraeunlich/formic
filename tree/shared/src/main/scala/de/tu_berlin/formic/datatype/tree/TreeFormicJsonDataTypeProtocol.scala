package de.tu_berlin.formic.datatype.tree

import de.tu_berlin.formic.common.datatype.{DataTypeName, DataTypeOperation}
import de.tu_berlin.formic.common.json.FormicJsonDataTypeProtocol
import upickle.Js
import upickle.Js.Value
import upickle.default._

/**
  * @author Ronny Bräunlich
  */
class TreeFormicJsonDataTypeProtocol[T](val name: DataTypeName)(implicit val reader: Reader[T], val writer: Writer[T]) extends FormicJsonDataTypeProtocol {

  implicit val treeNodeWriter = new TreeNodeWriter[T]

  override def deserializeOperation(json: String): DataTypeOperation = ???

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

class TreeNodeWriter[T]()(implicit val reader: Reader[T], val writer: Writer[T]) extends Writer[TreeNode] {
  override def write0: (TreeNode) => Value = {
    node =>
      Js.Obj(
        ("value", writeJs(node.value.asInstanceOf[T])),
        ("children", Js.Arr(node.children.map(this.write0): _*))
      )
  }
}