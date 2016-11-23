package de.tu_berlin.formic.common.json

import de.tu_berlin.formic.common.datatype.DataTypeName
import de.tu_berlin.formic.common.message._
import de.tu_berlin.formic.common.{ClientId, DataTypeInstanceId, OperationId}
import upickle.Js

/**
  * Due to the many possible DataTypeOperation subclasses that might exists this object is needed to handle
  * the correct de-/serialization. Data type implementations have to register their FormicJsonDataTypeProtocols
  * here. Those protocols are then used to properly serialize their operations. Code that wants to use
  * the serialization of an operation message has to import this object.
  *
  * @author Ronny Bräunlich
  */
object FormicJsonProtocol {
  private var dataTypeOperationJsonProtocols: Map[DataTypeName, FormicJsonDataTypeProtocol] = Map.empty

  def registerProtocol(protocol: FormicJsonDataTypeProtocol) = {
    dataTypeOperationJsonProtocols += (protocol.name -> protocol)
  }

  def remove(dataTypeName: DataTypeName) = dataTypeOperationJsonProtocols -= dataTypeName

  implicit val writer = upickle.default.Writer[OperationMessage] {
    case message =>
      val protocol = dataTypeOperationJsonProtocols.find(t => t._1 == message.dataType) match {
        case Some(prot) => prot
        case None => throw new IllegalArgumentException(s"No JSON Protocol for ${message.dataType} registered")
      }
      val jsonOperations = message.operations.map(o => protocol._2.serializeOperation(o)).map(json => upickle.json.read(json))
      Js.Obj(
        ("$type", Js.Str(classOf[OperationMessage].getName)),
        ("clientId", Js.Str(message.clientId.id)),
        ("dataTypeInstanceId", Js.Str(message.dataTypeInstanceId.id)),
        ("dataTypeName", Js.Str(message.dataType.name)),
        ("operations", Js.Arr(jsonOperations: _*)))

  }
  //I don't know why, but uPickle forces me to implement this for all types
  implicit val reader = upickle.default.Reader[FormicMessage] {
    case json: Js.Obj =>
      val map = json.obj
      val className = map("$type").str
      className match {
        case "de.tu_berlin.formic.common.message.CreateResponse" =>
          CreateResponse(DataTypeInstanceId(
            map("dataTypeInstanceId").obj("id").str)
          )
        case "de.tu_berlin.formic.common.message.CreateRequest" =>
          CreateRequest(
            ClientId(map("clientId").obj("id").str),
            DataTypeInstanceId(map("dataTypeInstanceId").obj("id").str),
            DataTypeName(map("dataType").obj("name").str)
          )
        case "de.tu_berlin.formic.common.message.HistoricOperationRequest" =>
          HistoricOperationRequest(
            ClientId(map("clientId").obj("id").str),
            DataTypeInstanceId(map("dataTypeInstanceId").obj("id").str),
            OperationId(map("sinceId").obj("id").str)
          )
        case "de.tu_berlin.formic.common.message.UpdateResponse" =>
          UpdateResponse(
            DataTypeInstanceId(map("dataTypeInstanceId").obj("id").str),
            DataTypeName(map("dataType").obj("name").str),
            map("data").str
          )
        case "de.tu_berlin.formic.common.message.UpdateRequest" =>
          UpdateRequest(
            ClientId(map("clientId").obj("id").str),
            DataTypeInstanceId(map("dataTypeInstanceId").obj("id").str)
          )
        case "de.tu_berlin.formic.common.message.OperationMessage" =>
          val protocol = dataTypeOperationJsonProtocols.find(t => t._1 == DataTypeName(map("dataTypeName").str)).get
          val operations = map("operations").arr.map(v => v.toString()).map(json => protocol._2.deserializeOperation(json)).toList
          OperationMessage(
            ClientId(map("clientId").str),
            DataTypeInstanceId(map("dataTypeInstanceId").str),
            DataTypeName(map("dataTypeName").str),
            operations)

      }
  }
}
