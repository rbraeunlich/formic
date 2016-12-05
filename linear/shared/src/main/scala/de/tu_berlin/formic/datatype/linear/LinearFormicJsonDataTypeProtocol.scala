package de.tu_berlin.formic.datatype.linear

import de.tu_berlin.formic.common.datatype.{DataTypeName, DataTypeOperation, OperationContext}
import de.tu_berlin.formic.common.json.FormicJsonDataTypeProtocol
import de.tu_berlin.formic.common.{ClientId, OperationId}
import upickle.Js
import upickle.default._

/**
  * @author Ronny Bräunlich
  */
class LinearFormicJsonDataTypeProtocol[T](val name: DataTypeName)(implicit val reader: Reader[T], val writer: Writer[T]) extends FormicJsonDataTypeProtocol {

  override def deserializeOperation(json: String): DataTypeOperation = {
    val valueMap = upickle.json.read(json).obj
    val operationId = OperationId(valueMap("operationId").str)
    val operationContext = valueMap("operationContext").arr.map(v => OperationId(v.str)).toList
    val clientId = ClientId(valueMap("clientId").str)
    val index = valueMap("index").num.toInt
    if (valueMap.contains("object")) {
      LinearInsertOperation(index, readJs[T](valueMap("object")).asInstanceOf[Object], operationId, OperationContext(operationContext), clientId)
    } else {
      LinearDeleteOperation(index, operationId, OperationContext(operationContext), clientId)
    }
  }

  override def serializeOperation(op: DataTypeOperation): String = {
    op match {
      case insert: LinearInsertOperation =>
        Js.Obj(
          ("index", Js.Num(insert.index)),
          ("object", writeJs(insert.o.asInstanceOf[T])),
          ("operationId", Js.Str(op.id.id)),
          ("operationContext", Js.Arr(op.operationContext.operations.map(o => Js.Str(o.id)): _*)),
          ("clientId", Js.Str(op.clientId.id))
        ).toString()
      case delete: LinearDeleteOperation =>
        Js.Obj(
          ("index", Js.Num(delete.index)),
          ("operationId", Js.Str(op.id.id)),
          ("operationContext", Js.Arr(op.operationContext.operations.map(o => Js.Str(o.id)): _*)),
          ("clientId", Js.Str(op.clientId.id))
        ).toString()
    }
  }
}
