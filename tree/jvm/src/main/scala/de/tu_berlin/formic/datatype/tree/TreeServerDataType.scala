package de.tu_berlin.formic.datatype.tree

import de.tu_berlin.formic.common.DataTypeInstanceId
import de.tu_berlin.formic.common.controlalgo.ControlAlgorithm
import de.tu_berlin.formic.common.datatype.{DataTypeName, DataTypeOperation, OperationTransformer}
import de.tu_berlin.formic.common.server.datatype.AbstractServerDataType
import upickle.default._

/**
  * @author Ronny Bräunlich
  */
class TreeServerDataType[T](id: DataTypeInstanceId, controlAlgorithm: ControlAlgorithm, val dataTypeName: DataTypeName)(implicit val writer: Writer[T]) extends AbstractServerDataType(id, controlAlgorithm) {

  implicit val treeNodeWriter = new ValueTreeNodeWriter[T]()

  override val transformer: OperationTransformer = new TreeTransformer

  override def apply(op: DataTypeOperation): Unit = {
    log.debug(s"Applying operation: $op")
    data = data.applyOperation(op.asInstanceOf[TreeStructureOperation])
  }

  var data: TreeNode = EmptyTreeNode

  override def getDataAsJson: String = {
    if(data == EmptyTreeNode) ""
    else write(data.asInstanceOf[ValueTreeNode])
  }
}

object TreeServerDataType {
  def apply[T](id: DataTypeInstanceId, controlAlgorithm: ControlAlgorithm, dataTypeName: DataTypeName)
              (implicit writer: Writer[T]): TreeServerDataType[T] = new TreeServerDataType(id, controlAlgorithm, dataTypeName)
}
