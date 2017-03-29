package de.tu_berlin.formic.datatype.tree.client

import akka.actor.ActorRef
import de.tu_berlin.formic.common.controlalgo.ControlAlgorithmClient
import de.tu_berlin.formic.common.datatype.client.AbstractClientDataStructure
import de.tu_berlin.formic.common.datatype.{DataStructureName, DataStructureOperation, OperationContext, OperationTransformer}
import de.tu_berlin.formic.common.{DataStructureInstanceId, OperationId}
import de.tu_berlin.formic.datatype.tree._
import upickle.default._
/**
  * @author Ronny Bräunlich
  */
class TreeClientDataStructure[T](id: DataStructureInstanceId,
                                 controlAlgorithm: ControlAlgorithmClient,
                                 val dataTypeName: DataStructureName,
                                 initialData: Option[String],
                                 lastOperationId: Option[OperationId],
                                 outgoingConnection: ActorRef)
                                (implicit val writer: Writer[T], val reader: Reader[T]) extends AbstractClientDataStructure(id, controlAlgorithm, lastOperationId, outgoingConnection) {

  implicit private val treeNodeReader = new ValueTreeNodeReader[T]()

  implicit private val treeNodeWriter = new ValueTreeNodeWriter[T]()

  private var privateData: TreeNode = initialData.map(read[ValueTreeNode]).getOrElse(EmptyTreeNode)

  def data: TreeNode = privateData

  override val transformer: OperationTransformer = new TreeTransformer

  override def apply(op: DataStructureOperation): Unit = {
    log.debug(s"Applying operation: $op")
    privateData = data.applyOperation(op.asInstanceOf[TreeStructureOperation])
  }

  override def cloneOperationWithNewContext(op: DataStructureOperation, context: OperationContext): DataStructureOperation = {
    op match {
      case TreeInsertOperation(path, tree, opId, _, clientId) => TreeInsertOperation(path, tree, opId, context, clientId)
      case TreeDeleteOperation(path, opId, _, clientId) => TreeDeleteOperation(path, opId, context, clientId)
    }
  }

  override def getDataAsJson: String = {
    if (data == EmptyTreeNode) ""
    else write[ValueTreeNode](data.asInstanceOf[ValueTreeNode])
  }
}

object TreeClientDataStructure {
  def apply[T](id: DataStructureInstanceId, controlAlgorithm: ControlAlgorithmClient, dataTypeName: DataStructureName, initialData: Option[String], lastOperationId: Option[OperationId], outgoingConnection: ActorRef)(implicit writer: Writer[T], reader: Reader[T]): TreeClientDataStructure[T] =
    new TreeClientDataStructure(id, controlAlgorithm, dataTypeName, initialData, lastOperationId, outgoingConnection)

}