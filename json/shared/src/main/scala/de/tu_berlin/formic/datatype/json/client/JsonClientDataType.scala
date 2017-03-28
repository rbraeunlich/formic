package de.tu_berlin.formic.datatype.json.client

import akka.actor.ActorRef
import de.tu_berlin.formic.common.controlalgo.ControlAlgorithmClient
import de.tu_berlin.formic.common.datatype.FormicDataType.LocalOperationMessage
import de.tu_berlin.formic.common.datatype.client.AbstractClientDataType
import de.tu_berlin.formic.common.datatype.{DataStructureName, DataTypeOperation, OperationContext, OperationTransformer}
import de.tu_berlin.formic.common.message.{FormicMessage, OperationMessage}
import de.tu_berlin.formic.common.{ClientId, DataStructureInstanceId$, OperationId}
import de.tu_berlin.formic.datatype.json._
import de.tu_berlin.formic.datatype.json.client.JsonClientDataType._
import de.tu_berlin.formic.datatype.tree.{TreeDeleteOperation, TreeInsertOperation, TreeStructureOperation}
import upickle.default._
import de.tu_berlin.formic.datatype.json.JsonFormicJsonDataTypeProtocol._

/**
  * @author Ronny Bräunlich
  */
class JsonClientDataType(id: DataStructureInstanceId,
                         controlAlgorithm: ControlAlgorithmClient,
                         val dataTypeName: DataStructureName,
                         initialData: Option[String],
                         lastOperationId: Option[OperationId],
                         outgoingConnection: ActorRef)
                        (implicit val writer: JsonTreeNodeWriter, val reader: JsonTreeNodeReader) extends AbstractClientDataType(id, controlAlgorithm, lastOperationId, outgoingConnection) {

  private var privateData: ObjectNode = initialData.map(read[ObjectNode]).getOrElse(ObjectNode(null, List.empty))

  def data: ObjectNode = privateData

  override val transformer: OperationTransformer = new JsonTransformer

  override def apply(op: DataTypeOperation): Unit = {
    log.debug(s"Applying operation: $op")
    privateData = data.applyOperation(op.asInstanceOf[TreeStructureOperation]).asInstanceOf[ObjectNode]
  }

  override def cloneOperationWithNewContext(op: DataTypeOperation, context: OperationContext): DataTypeOperation = {
    op match {
      case TreeInsertOperation(path, tree, opId, _, clientId) => TreeInsertOperation(path, tree, opId, context, clientId)
      case TreeDeleteOperation(path, opId, _, clientId) => TreeDeleteOperation(path, opId, context, clientId)
      case JsonReplaceOperation(path, tree, opId, _, clientId) => JsonReplaceOperation(path, tree, opId, context, clientId)
    }
  }

  override def getDataAsJson: String = write(data)

  override def unacknowledged(callbackWrapper: _root_.akka.actor.ActorRef): JsonClientDataType.this.Receive = {
    case local: LocalOperationMessage =>
      local.op.operations.head match {
        case json: JsonClientOperation =>
          val newOperation = transformJsonOperationsIntoGeneralTreeOperations(json)
          val newMessage = OperationMessage(local.op.clientId, local.op.dataTypeInstanceId, local.op.dataType, List(newOperation))
          super.unacknowledged(callbackWrapper).apply(LocalOperationMessage(newMessage))
        case _ => super.unacknowledged(callbackWrapper).apply(local)
      }
    case rest: FormicMessage => super.unacknowledged(callbackWrapper).apply(rest)
  }

  override def acknowledged(callbackWrapper: ActorRef): Receive = {
    case local: LocalOperationMessage =>
      local.op.operations.head match {
        case json: JsonClientOperation =>
          val newOperation = transformJsonOperationsIntoGeneralTreeOperations(json)
          val newMessage = OperationMessage(local.op.clientId, local.op.dataTypeInstanceId, local.op.dataType, List(newOperation))
          super.acknowledged(callbackWrapper).apply(LocalOperationMessage(newMessage))
        case _ => super.acknowledged(callbackWrapper).apply(local)
      }
    case rest: FormicMessage => super.acknowledged(callbackWrapper).apply(rest)
  }

  /**
    * Because the user is allowed to use a JSON path, the JSONClientOperations have to be translated into
    * basic tree operations including the translation of the path.
    */
  def transformJsonOperationsIntoGeneralTreeOperations(jsonOp: JsonClientOperation): TreeStructureOperation = {
    val newOperation = jsonOp match {
      case ins: JsonClientInsertOperation => TreeInsertOperation(data.translateJsonPathForInsertion(jsonOp.path), ins.tree, ins.id, ins.operationContext, ins.clientId)
      case del: JsonClientDeleteOperation => TreeDeleteOperation(data.translateJsonPath(jsonOp.path), del.id, del.operationContext, del.clientId)
      case rep: JsonClientReplaceOperation => JsonReplaceOperation(data.translateJsonPath(jsonOp.path), rep.tree, rep.id, rep.operationContext, rep.clientId)
    }
    newOperation
  }
}

object JsonClientDataType {

  /**
    * Marker interface for all the operations that are only restricted to the client and to the JSON data type.
    * Common for all of them is the JsonPath that has to be translated into an AccessPath.
    */
  sealed trait JsonClientOperation extends DataTypeOperation {
    val path: JsonPath
  }

  case class JsonClientInsertOperation(path: JsonPath, tree: JsonTreeNode[_], id: OperationId, operationContext: OperationContext, var clientId: ClientId) extends JsonClientOperation

  case class JsonClientDeleteOperation(path: JsonPath, id: OperationId, operationContext: OperationContext, var clientId: ClientId) extends JsonClientOperation

  case class JsonClientReplaceOperation(path: JsonPath, tree: JsonTreeNode[_], id: OperationId, operationContext: OperationContext, var clientId: ClientId) extends JsonClientOperation

  def apply(id: DataStructureInstanceId,
            controlAlgorithm: ControlAlgorithmClient,
            dataTypeName: DataStructureName,
            initialData: Option[String],
            lastOperationId: Option[OperationId],
            outgoingConnection: ActorRef): JsonClientDataType = new JsonClientDataType(id, controlAlgorithm, dataTypeName, initialData, lastOperationId, outgoingConnection)
}
