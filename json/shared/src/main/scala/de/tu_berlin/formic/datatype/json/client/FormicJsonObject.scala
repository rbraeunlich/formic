package de.tu_berlin.formic.datatype.json.client

import akka.actor.ActorRef
import akka.pattern._
import akka.util.Timeout
import de.tu_berlin.formic.common.datatype.FormicDataType.LocalOperationMessage
import de.tu_berlin.formic.common.datatype.client.{ClientDataTypeEvent, DataTypeInitiator}
import de.tu_berlin.formic.common.datatype.{FormicDataType, OperationContext}
import de.tu_berlin.formic.common.message.{OperationMessage, UpdateRequest, UpdateResponse}
import de.tu_berlin.formic.common.{ClientId, DataStructureInstanceId, OperationId}
import de.tu_berlin.formic.datatype.json.JsonFormicJsonDataTypeProtocol._
import de.tu_berlin.formic.datatype.json._
import de.tu_berlin.formic.datatype.json.client.JsonClientDataType._
import upickle.default._

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js.annotation.JSExportAll

/**
  * @author Ronny Bräunlich
  */
@JSExportAll
class FormicJsonObject(callback: (ClientDataTypeEvent) => Unit,
                       initiator: DataTypeInitiator,
                       dataTypeInstanceId: DataStructureInstanceId = DataStructureInstanceId())
  extends FormicDataType(callback, FormicJsonObjectFactory.name, dataStructureInstanceId = dataTypeInstanceId, initiator = initiator) {

  implicit val timeout: Timeout = 1.seconds

  def this(callback: (ClientDataTypeEvent) => Unit, initiator: DataTypeInitiator, dataTypeInstanceId: DataStructureInstanceId, wrapped: ActorRef, localClientId: ClientId) {
    this(callback, initiator, dataTypeInstanceId)
    this.actor = wrapped
    this.clientId = localClientId
  }

  def insert(i: Double, path: JsonPath): OperationId = {
    val toInsert = NumberNode(path.path.last, i)
    sendInsertOperation(toInsert, path)
  }

  def insert(s: String, path: JsonPath): OperationId = {
    val chars = s.toCharArray.map(ch => CharacterNode(null, ch)).toList
    val toInsert = StringNode(path.path.last, chars)
    sendInsertOperation(toInsert, path)
  }

  def insert(c: Char, path: JsonPath): OperationId = {
    val toInsert = CharacterNode(null, c)
    sendInsertOperation(toInsert, path)
  }

  def insert(b: Boolean, path: JsonPath): OperationId = {
    val toInsert = BooleanNode(path.path.last, b)
    sendInsertOperation(toInsert, path)
  }

  def insert[T](obj: T, path: JsonPath)(implicit writer: Writer[T]): OperationId = {
    val toInsert: JsonTreeNode[_] = createNodeForObject(obj, path)
    sendInsertOperation(toInsert, path)
  }

  private def sendInsertOperation(toInsert: JsonTreeNode[_], path: JsonPath): OperationId = {
    val opId = OperationId()
    actor ! LocalOperationMessage(
      OperationMessage(clientId, dataTypeInstanceId, dataStructureName, List(
        JsonClientInsertOperation(path, toInsert, opId, OperationContext(), clientId)
      ))
    )
    opId
  }

  def remove(path: JsonPath): OperationId = {
    val opId = OperationId()
    actor ! LocalOperationMessage(
      OperationMessage(clientId, dataTypeInstanceId, dataStructureName, List(
        JsonClientDeleteOperation(path, opId, OperationContext(), clientId)
      ))
    )
    opId
  }

  def replace(i: Double, path: JsonPath): OperationId = {
    val replacement = NumberNode(path.path.last, i)
    sendReplaceOperation(replacement, path)
  }

  def replace(s: String, path: JsonPath): OperationId = {
    val chars = s.toCharArray.map(ch => CharacterNode(null, ch)).toList
    val replacement = StringNode(path.path.last, chars)
    sendReplaceOperation(replacement, path)
  }

  def replace(b: Boolean, path: JsonPath): OperationId = {
    val replacement = BooleanNode(path.path.last, b)
    sendReplaceOperation(replacement, path)
  }

  def replace(c: Char, path: JsonPath): OperationId = {
    val replacement = CharacterNode(null, c)
    sendReplaceOperation(replacement, path)
  }

  def replace[T](obj: T, path: JsonPath)(implicit writer: Writer[T]): OperationId = {
    val toInsert: JsonTreeNode[_] = createNodeForObject(obj, path)
    sendReplaceOperation(toInsert, path)
  }

  private def createNodeForObject[T](obj: T, path: JsonPath)(implicit writer: Writer[T]): JsonTreeNode[_] = {
    //because of the overloaded methods, T can only be an array or any object
    val isArray = obj.isInstanceOf[Array[_]] || obj.isInstanceOf[Seq[_]]
    //here we use the fact that any arbitrary JSON object can be represented as
    //JSON tree (that is actually the intention of a JSON tree)
    val originalJson = write(obj)
    //if we were given a simple array, we just place it inside an object so we can read it as ObjectNode
    val intermediate = read[ObjectNode](if (isArray) s"""{"${path.path.last}":$originalJson}""" else originalJson)
    //we have to set the key explicitely
    val toInsert = if (isArray) intermediate.children.head else ObjectNode(path.path.last, intermediate.children)
    toInsert
  }

  private def sendReplaceOperation(toInsert: JsonTreeNode[_], path: JsonPath): OperationId = {
    val opId = OperationId()
    actor ! LocalOperationMessage(
      OperationMessage(clientId, dataTypeInstanceId, dataStructureName, List(
        JsonClientReplaceOperation(path, toInsert, opId, OperationContext(), clientId)
      ))
    )
    opId
  }

  def getNodeAt(path: JsonPath)(implicit ec: ExecutionContext): Future[JsonTreeNode[_]] = {
    ask(actor, UpdateRequest(clientId, dataTypeInstanceId)).
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
    ask(actor, UpdateRequest(clientId, dataTypeInstanceId)).
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
