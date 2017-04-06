package de.tu_berlin.formic.datastructure.json.client

import akka.actor.ActorRef
import de.tu_berlin.formic.common.controlalgo.ControlAlgorithmClient
import de.tu_berlin.formic.common.datastructure.FormicDataStructure.LocalOperationMessage
import de.tu_berlin.formic.common.datastructure.client.AbstractClientDataStructure
import de.tu_berlin.formic.common.datastructure.{DataStructureName, DataStructureOperation, OperationContext, OperationTransformer}
import de.tu_berlin.formic.common.message.{FormicMessage, OperationMessage}
import de.tu_berlin.formic.common.{ClientId, DataStructureInstanceId, OperationId}
import de.tu_berlin.formic.datastructure.json._
import de.tu_berlin.formic.datastructure.json.client.JsonClientDataStructure._
import de.tu_berlin.formic.datastructure.json.JsonFormicJsonDataStructureProtocol._
import de.tu_berlin.formic.datastructure.tree.{TreeDeleteOperation, TreeInsertOperation, TreeStructureOperation}
import upickle.default._

/**
  * @author Ronny Bräunlich
  */
class JsonClientDataStructure(id: DataStructureInstanceId,
                              controlAlgorithm: ControlAlgorithmClient,
                              val dataStructureName: DataStructureName,
                              initialData: Option[String],
                              lastOperationId: Option[OperationId],
                              outgoingConnection: ActorRef)
                             (implicit val writer: JsonTreeNodeWriter, val reader: JsonTreeNodeReader) extends AbstractClientDataStructure(id, controlAlgorithm, lastOperationId, outgoingConnection) {

  private var privateData: ObjectNode = initialData.map(read[ObjectNode]).getOrElse(ObjectNode(null, List.empty))

  def data: ObjectNode = privateData

  override val transformer: OperationTransformer = new JsonTransformer

  override def apply(op: DataStructureOperation): Unit = {
    log.debug(s"Applying operation: $op")
    privateData = data.applyOperation(op.asInstanceOf[TreeStructureOperation]).asInstanceOf[ObjectNode]
  }

  override def cloneOperationWithNewContext(op: DataStructureOperation, context: OperationContext): DataStructureOperation = {
    op match {
      case TreeInsertOperation(path, tree, opId, _, clientId) => TreeInsertOperation(path, tree, opId, context, clientId)
      case TreeDeleteOperation(path, opId, _, clientId) => TreeDeleteOperation(path, opId, context, clientId)
      case JsonReplaceOperation(path, tree, opId, _, clientId) => JsonReplaceOperation(path, tree, opId, context, clientId)
    }
  }

  override def getDataAsJson: String = write(data)

  override def unacknowledged(callbackWrapper: _root_.akka.actor.ActorRef): JsonClientDataStructure.this.Receive = {
    case local: LocalOperationMessage =>
      local.op.operations.head match {
        case json: JsonClientOperation =>
          val newOperation = transformJsonOperationsIntoGeneralTreeOperations(json)
          val newMessage = OperationMessage(local.op.clientId, local.op.dataStructureInstanceId, local.op.dataStructure, List(newOperation))
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
          val newMessage = OperationMessage(local.op.clientId, local.op.dataStructureInstanceId, local.op.dataStructure, List(newOperation))
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

object JsonClientDataStructure {

  /**
    * Marker interface for all the operations that are only restricted to the client and to the JSON data type.
    * Common for all of them is the JsonPath that has to be translated into an AccessPath.
    */
  sealed trait JsonClientOperation extends DataStructureOperation {
    val path: JsonPath
  }

  case class JsonClientInsertOperation(path: JsonPath, tree: JsonTreeNode[_], id: OperationId, operationContext: OperationContext, var clientId: ClientId) extends JsonClientOperation

  case class JsonClientDeleteOperation(path: JsonPath, id: OperationId, operationContext: OperationContext, var clientId: ClientId) extends JsonClientOperation

  case class JsonClientReplaceOperation(path: JsonPath, tree: JsonTreeNode[_], id: OperationId, operationContext: OperationContext, var clientId: ClientId) extends JsonClientOperation

  def apply(id: DataStructureInstanceId,
            controlAlgorithm: ControlAlgorithmClient,
            dataStructureName: DataStructureName,
            initialData: Option[String],
            lastOperationId: Option[OperationId],
            outgoingConnection: ActorRef): JsonClientDataStructure = new JsonClientDataStructure(id, controlAlgorithm, dataStructureName, initialData, lastOperationId, outgoingConnection)
}
