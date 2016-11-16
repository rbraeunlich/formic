package de.tu_berlin.formic.common.message

import de.tu_berlin.formic.common.datatype.{DataTypeName, DataTypeOperation}
import de.tu_berlin.formic.common.json.FormicJsonDataTypeProtocol
import de.tu_berlin.formic.common.{ClientId, DataTypeInstanceId, OperationId}
import upickle.Js

/**
  * @author Ronny Bräunlich
  */
sealed trait FormicMessage

/**
  * A response from the server, indicating that a data type instance has been created.
  *
  * @author Ronny Bräunlich
  */
case class CreateResponse(dataTypeInstanceId: DataTypeInstanceId) extends FormicMessage

case class CreateRequest(clientId: ClientId, dataTypeInstanceId: DataTypeInstanceId, dataType: DataTypeName) extends FormicMessage

case class HistoricOperationRequest(clientId: ClientId, dataTypeInstanceId: DataTypeInstanceId, sinceId: OperationId) extends FormicMessage

case class UpdateResponse(dataTypeInstanceId: DataTypeInstanceId, dataType: DataTypeName, data: String) extends FormicMessage

case class UpdateRequest(clientId: ClientId, dataTypeInstanceId: DataTypeInstanceId) extends FormicMessage

case class OperationMessage(clientId: ClientId, dataTypeInstanceId: DataTypeInstanceId, dataType: DataTypeName, operations: List[DataTypeOperation]) extends FormicMessage

/**
  * Due to the many possible DataTypeOperation subclasses that might exists this object is needed to handle
  * the correct de-/serialization. Data type implementations have to register their FormicJsonDataTypeProtocols
  * here. Those protocols are then used to properly serialize their operations. Code that wants to use
  * the serialization of an operation message has to import this object.
  */
object OperationMessage {

  private var dataTypeOperationJsonProtocols: Map[DataTypeName, FormicJsonDataTypeProtocol] = Map.empty

  def registerProtocol(protocol: FormicJsonDataTypeProtocol) = {
    dataTypeOperationJsonProtocols += (protocol.name -> protocol)
  }

  def clear() = dataTypeOperationJsonProtocols = Map.empty

  implicit val writer = upickle.default.Writer[OperationMessage] {
    case message =>
      val protocol = dataTypeOperationJsonProtocols.find(t => t._1.equals(message.dataType)).get
      val jsonOperations = message.operations.map(o => protocol._2.serializeOperation(o)).map(json => upickle.json.read(json))
      Js.Obj(
        ("$type", Js.Str(OperationMessage.getClass.getName)),
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
      println(className)
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
          val protocol = dataTypeOperationJsonProtocols.find(t => t._1.equals(DataTypeName(map("dataTypeName").str))).get
          val operations = map("operations").arr.map(v => v.toString()).map(json => protocol._2.deserializeOperation(json)).toList
          OperationMessage(
            ClientId(map("clientId").str),
            DataTypeInstanceId(map("dataTypeInstanceId").str),
            DataTypeName(map("dataTypeName").str),
            operations)

      }

  }
}