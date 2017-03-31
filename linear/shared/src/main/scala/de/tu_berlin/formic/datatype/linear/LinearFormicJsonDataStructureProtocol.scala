package de.tu_berlin.formic.datatype.linear

import de.tu_berlin.formic.common.datastructure.{DataStructureName, DataStructureOperation, OperationContext}
import de.tu_berlin.formic.common.json.FormicJsonDataStructureProtocol
import de.tu_berlin.formic.common.{ClientId, OperationId}
import upickle.Js
import upickle.default._

/**
  * @author Ronny Bräunlich
  */
case class LinearFormicJsonDataStructureProtocol[T](name: DataStructureName)(implicit val reader: Reader[T], val writer: Writer[T]) extends FormicJsonDataStructureProtocol {

  override def deserializeOperation(json: String): DataStructureOperation = {
    val valueMap = upickle.json.read(json).obj
    val operationId = OperationId(valueMap("operationId").str)
    val operationContext = valueMap("operationContext").arr.map(v => OperationId(v.str)).toList
    val clientId = ClientId(valueMap("clientId").str)
    val index = valueMap("index").num.toInt
    if (valueMap.contains("object")) {
      LinearInsertOperation(index, readJs[T](valueMap("object")).asInstanceOf[Object], operationId, OperationContext(operationContext), clientId)
    } else if(index > -1 ) {
      LinearDeleteOperation(index, operationId, OperationContext(operationContext), clientId)
    } else {
      LinearNoOperation(operationId, OperationContext(operationContext), clientId)
    }
  }

  override def serializeOperation(op: DataStructureOperation): String = {
    op match {
      case insert: LinearInsertOperation =>
        write(Js.Obj(
          ("index", Js.Num(insert.index)),
          ("object", writeJs(insert.o.asInstanceOf[T])),
          ("operationId", Js.Str(op.id.id)),
          ("operationContext", Js.Arr(op.operationContext.operations.map(o => Js.Str(o.id)): _*)),
          ("clientId", Js.Str(op.clientId.id))
        ))

      case delete: LinearDeleteOperation =>
        write(Js.Obj(
          ("index", Js.Num(delete.index)),
          ("operationId", Js.Str(op.id.id)),
          ("operationContext", Js.Arr(op.operationContext.operations.map(o => Js.Str(o.id)): _*)),
          ("clientId", Js.Str(op.clientId.id))
        ))

      case no: LinearNoOperation =>
        write(Js.Obj(
          ("index", Js.Num(no.index)),
          ("operationId", Js.Str(op.id.id)),
          ("operationContext", Js.Arr(op.operationContext.operations.map(o => Js.Str(o.id)): _*)),
          ("clientId", Js.Str(op.clientId.id))
        ))
    }
  }
}
