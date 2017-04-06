package de.tu_berlin.formic.common.json

import de.tu_berlin.formic.common.datastructure.DataStructureName
import de.tu_berlin.formic.common.message._
import de.tu_berlin.formic.common.{ClientId, DataStructureInstanceId, OperationId}
import upickle.Js

/**
  * Due to the many possible DataTypeOperation subclasses that might exists this object is needed to handle
  * the correct de-/serialization. Data type implementations have to register their FormicJsonDataTypeProtocols
  * here. Those protocols are then used to properly serialize their operations. Code that wants to use
  * the serialization of an operation message has to import the writer and reader vals.
  *
  * @author Ronny Bräunlich
  */
class FormicJsonProtocol {
  private var _dataTypeOperationJsonProtocols: Map[DataStructureName, FormicJsonDataStructureProtocol] = Map.empty

  def dataStructureOperationJsonProtocols: Map[DataStructureName, FormicJsonDataStructureProtocol] = Map(_dataTypeOperationJsonProtocols.toList:_*)

  def registerProtocol(protocol: FormicJsonDataStructureProtocol) = {
    _dataTypeOperationJsonProtocols += (protocol.name -> protocol)
  }

  def remove(dataTypeName: DataStructureName) = _dataTypeOperationJsonProtocols -= dataTypeName

  implicit val writer = upickle.default.Writer[OperationMessage] {
    message =>
      val protocol = _dataTypeOperationJsonProtocols.find(t => t._1 == message.dataStructure) match {
        case Some(prot) => prot
        case None => throw new IllegalArgumentException(s"No JSON Protocol for ${message.dataStructure} registered")
      }
      val jsonOperations = message.operations.map(o => protocol._2.serializeOperation(o)).map(json => upickle.json.read(json))
      Js.Obj(
        ("$type", Js.Str(classOf[OperationMessage].getName)),
        ("clientId", Js.Str(message.clientId.id)),
        ("dataStructureInstanceId", Js.Str(message.dataStructureInstanceId.id)),
        ("dataStructureName", Js.Str(message.dataStructure.name)),
        ("operations", Js.Arr(jsonOperations: _*)))

  }
  //I don't know why, but uPickle forces me to implement this for all types
  implicit val reader = upickle.default.Reader[FormicMessage] {
    case json: Js.Obj =>
      val map = json.obj
      val className = map("$type").str
      className match {
        case "de.tu_berlin.formic.common.message.CreateResponse" =>
          CreateResponse(DataStructureInstanceId(
            map("dataStructureInstanceId").obj("id").str)
          )
        case "de.tu_berlin.formic.common.message.CreateRequest" =>
          CreateRequest(
            ClientId(map("clientId").obj("id").str),
            DataStructureInstanceId(map("dataStructureInstanceId").obj("id").str),
            DataStructureName(map("dataStructure").obj("name").str)
          )
        case "de.tu_berlin.formic.common.message.HistoricOperationRequest" =>
          HistoricOperationRequest(
            ClientId(map("clientId").obj("id").str),
            DataStructureInstanceId(map("dataStructureInstanceId").obj("id").str),
            map("sinceId") match {
              case Js.Null => null
              case obj:Js.Obj => OperationId(obj("id").str)
            })
        case "de.tu_berlin.formic.common.message.UpdateResponse" =>
          UpdateResponse(
            DataStructureInstanceId(map("dataStructureInstanceId").obj("id").str),
            DataStructureName(map("dataStructure").obj("name").str),
            map("data").str,
            map("lastOperationId").arr.headOption.map(value => OperationId(value.obj("id").str))
          )
        case "de.tu_berlin.formic.common.message.UpdateRequest" =>
          UpdateRequest(
            ClientId(map("clientId").obj("id").str),
            DataStructureInstanceId(map("dataStructureInstanceId").obj("id").str)
          )
        case "de.tu_berlin.formic.common.message.OperationMessage" =>
          val protocol = _dataTypeOperationJsonProtocols.find(t => t._1 == DataStructureName(map("dataStructureName").str)).get
          val operations = map("operations").arr.map(v => v.toString()).map(json => protocol._2.deserializeOperation(json)).toList
          OperationMessage(
            ClientId(map("clientId").str),
            DataStructureInstanceId(map("dataStructureInstanceId").str),
            DataStructureName(map("dataStructureName").str),
            operations)
      }
  }
}

object FormicJsonProtocol {
  def apply(): FormicJsonProtocol = new FormicJsonProtocol()
}
