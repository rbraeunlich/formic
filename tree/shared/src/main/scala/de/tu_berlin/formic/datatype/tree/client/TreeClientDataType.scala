package de.tu_berlin.formic.datatype.tree.client

import akka.actor.ActorRef
import de.tu_berlin.formic.common.controlalgo.ControlAlgorithmClient
import de.tu_berlin.formic.common.datatype.client.AbstractClientDataType
import de.tu_berlin.formic.common.datatype.{DataTypeName, DataTypeOperation, OperationContext, OperationTransformer}
import de.tu_berlin.formic.common.{DataTypeInstanceId, OperationId}
import de.tu_berlin.formic.datatype.tree._
import upickle.default._
/**
  * @author Ronny Bräunlich
  */
class TreeClientDataType[T](id: DataTypeInstanceId,
                            controlAlgorithm: ControlAlgorithmClient,
                            val dataTypeName: DataTypeName,
                            initialData: Option[String],
                            lastOperationId: Option[OperationId],
                            outgoingConnection: ActorRef)
                           (implicit val writer: Writer[T], val reader: Reader[T]) extends AbstractClientDataType(id, controlAlgorithm, lastOperationId, outgoingConnection) {

  implicit private val treeNodeReader = new ValueTreeNodeReader[T]()

  implicit private val treeNodeWriter = new ValueTreeNodeWriter[T]()

  private var privateData: TreeNode = initialData.map(read[ValueTreeNode]).getOrElse(EmptyTreeNode)

  def data: TreeNode = privateData

  override val transformer: OperationTransformer = new TreeTransformer

  override def apply(op: DataTypeOperation): Unit = {
    log.debug(s"Applying operation: $op")
    privateData = data.applyOperation(op.asInstanceOf[TreeStructureOperation])
  }

  override def cloneOperationWithNewContext(op: DataTypeOperation, context: OperationContext): DataTypeOperation = {
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

object TreeClientDataType {
  def apply[T](id: DataTypeInstanceId, controlAlgorithm: ControlAlgorithmClient, dataTypeName: DataTypeName, initialData: Option[String], lastOperationId: Option[OperationId], outgoingConnection: ActorRef)(implicit writer: Writer[T], reader: Reader[T]): TreeClientDataType[T] =
    new TreeClientDataType(id, controlAlgorithm, dataTypeName, initialData, lastOperationId, outgoingConnection)

}