package de.tu_berlin.formic.datatype.linear

import de.tu_berlin.formic.common.datatype.{DataTypeName, DataTypeOperation, OperationContext}
import de.tu_berlin.formic.common.json.FormicJsonDataTypeProtocol
import de.tu_berlin.formic.common.{ClientId, OperationId}
import upickle.Js
import upickle.default._

/**
  * @author Ronny Bräunlich
  */
object LinearFormicJsonDataTypeProtocol extends FormicJsonDataTypeProtocol {

  override val name: DataTypeName = LinearServerDataType.dataTypeName

  override def deserializeOperation(json: String): DataTypeOperation = {
    val valueMap = upickle.json.read(json).obj
    val operationId = OperationId(valueMap("operationId").str)
    val operationContext = valueMap("operationContext").arr.map(v => OperationId(v.str)).toList
    val clientId = ClientId(valueMap("clientId").str)
    val index = valueMap("index").num.toInt
    if (valueMap.contains("object")) {
      //FIXME
      LinearInsertOperation(index, valueMap("object").str, operationId, OperationContext(operationContext), clientId)
    } else {
      LinearDeleteOperation(index, operationId, OperationContext(operationContext), clientId)
    }
  }

  override def serializeOperation(op: DataTypeOperation): String = {
    op match {
      case insert: LinearInsertOperation =>
        Js.Obj(
          ("index", Js.Num(insert.index)),
          ("object", Js.Str(insert.o.toString)), //FIXME
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
