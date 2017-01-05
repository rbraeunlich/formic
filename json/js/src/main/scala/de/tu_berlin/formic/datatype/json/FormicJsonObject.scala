package de.tu_berlin.formic.datatype.json

import akka.actor.ActorRef
import akka.pattern._
import akka.util.Timeout
import de.tu_berlin.formic.common.datatype.FormicDataType.LocalOperationMessage
import de.tu_berlin.formic.common.datatype.client.DataTypeInitiator
import de.tu_berlin.formic.common.datatype.{FormicDataType, OperationContext}
import de.tu_berlin.formic.common.message.{OperationMessage, UpdateRequest, UpdateResponse}
import de.tu_berlin.formic.common.{DataTypeInstanceId, OperationId}
import de.tu_berlin.formic.datatype.json.JsonClientDataType.{JsonClientDeleteOperation, JsonClientInsertOperation, JsonClientReplaceOperation}
import upickle.default._
import de.tu_berlin.formic.datatype.json.JsonFormicJsonDataTypeProtocol._

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._
import scala.scalajs.js.annotation.JSExportAll

/**
  * @author Ronny Bräunlich
  */
@JSExportAll
class FormicJsonObject(callback: () => Unit,
                       initiator: DataTypeInitiator,
                       dataTypeInstanceId: DataTypeInstanceId) extends FormicDataType(callback, FormicJsonObjectFactory.name, dataTypeInstanceId = dataTypeInstanceId, initiator = initiator) {

  implicit val timeout: Timeout = 1.seconds

  def this(callback: () => Unit, initiator: DataTypeInitiator, dataTypeInstanceId: DataTypeInstanceId, wrapped: ActorRef) {
    this(callback, initiator, dataTypeInstanceId)
    this.actor = wrapped
  }
  def insert(i: Double, path: JsonPath) = {
    val toInsert = NumberNode(path.path.last, i)
    sendInsertOperation(toInsert, path)
  }

  def insert(s: String, path: JsonPath) = {
    val chars = s.toCharArray.map(ch => CharacterNode(null, ch)).toList
    val toInsert = StringNode(path.path.last, chars)
    sendInsertOperation(toInsert, path)
  }

  def insert(c: Char, path: JsonPath) = {
    val toInsert = CharacterNode(null, c)
    sendInsertOperation(toInsert, path)
  }

  def insert(b: Boolean, path: JsonPath) = {
    val toInsert = BooleanNode(path.path.last, b)
    sendInsertOperation(toInsert, path)
  }

  /**
    * Due to collisions, this method cannot follow the simple insert() syntax
    */
  def insertArray[T](arr: Array[T], path: JsonPath)(implicit writer: Writer[T]) = {
    val children = arr.map(elem => read[ObjectNode](write(elem)))
    val toInsert = ArrayNode(path.path.last, children.toList)
    sendInsertOperation(toInsert, path)
  }

  def insert[T](obj: T, path: JsonPath)(implicit writer: Writer[T]) = {
    //here we use the fact that any arbitrary JSON object can be represented as
    //JSON tree (that is actually the intention of a JSON tree)
    val originalJson = write(obj)
    val toInsert = read[ObjectNode](originalJson)
    sendInsertOperation(toInsert, path)
  }

  def sendInsertOperation(toInsert: JsonTreeNode[_], path: JsonPath) = {
    actor ! LocalOperationMessage(
      OperationMessage(null, dataTypeInstanceId, dataTypeName, List(
        JsonClientInsertOperation(path, toInsert, OperationId(), OperationContext(), null)
      ))
    )
  }

  def remove(path: JsonPath) = {
    actor ! LocalOperationMessage(
      OperationMessage(null, dataTypeInstanceId, dataTypeName, List(
        JsonClientDeleteOperation(path, OperationId(), OperationContext(), null)
      ))
    )
  }

  def replace(i: Double, path: JsonPath) = {
    val replacement = NumberNode(path.path.last, i)
    sendReplaceOperation(replacement, path)
  }

  def replace(s: String, path: JsonPath) = {
    val chars = s.toCharArray.map(ch => CharacterNode(null, ch)).toList
    val replacement = StringNode(path.path.last, chars)
    sendReplaceOperation(replacement, path)
  }

  def replace(b: Boolean, path: JsonPath) = {
    val replacement = BooleanNode(path.path.last, b)
    sendReplaceOperation(replacement, path)
  }

  def replace(c: Char, path: JsonPath) = {
    val replacement = CharacterNode(null, c)
    sendReplaceOperation(replacement, path)
  }

  def replace[T](obj: T, path: JsonPath)(implicit writer: Writer[T]) = {
    //here we use the fact that any arbitrary JSON object can be represented as
    //JSON tree (that is actually the intention of a JSON tree)
    val originalJson = write(obj)
    val toInsert = read[ObjectNode](originalJson)
    sendReplaceOperation(toInsert, path)
  }

  def sendReplaceOperation(toInsert: JsonTreeNode[_], path: JsonPath) = {
    actor ! LocalOperationMessage(
      OperationMessage(null, dataTypeInstanceId, dataTypeName, List(
        JsonClientReplaceOperation(path, toInsert, OperationId(), OperationContext(), null)
      ))
    )
  }

  def getNodeAt(path: JsonPath)(implicit ec: ExecutionContext): Future[JsonTreeNode[_]] = {
    ask(actor, UpdateRequest(null, dataTypeInstanceId)).
      mapTo[UpdateResponse].
      map(rep => {
        rep.data
      }).
      map(data => read[ObjectNode](data)
      ).
      map(json => {
        val accessPath = json.translateJsonPath(path)
        json.getNode(accessPath).asInstanceOf[JsonTreeNode[_]]
      })
  }

  def getValueAt[T](path: JsonPath)(implicit ec: ExecutionContext): Future[T] = {
    ask(actor, UpdateRequest(null, dataTypeInstanceId)).
      mapTo[UpdateResponse].
      map(rep => {
        rep.data
      }).
      map(data => read[ObjectNode](data)
      ).
      map(json => {
        val accessPath = json.translateJsonPath(path)
        json.getNode(accessPath).getData.asInstanceOf[T]
      })
  }
}
