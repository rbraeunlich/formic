package de.tu_berlin.formic.datatype.tree

import de.tu_berlin.formic.common.DataStructureInstanceId
import de.tu_berlin.formic.common.controlalgo.ControlAlgorithm
import de.tu_berlin.formic.common.datatype.{DataStructureName, DataStructureOperation, OperationTransformer}
import de.tu_berlin.formic.common.server.datatype.AbstractServerDataStructure
import upickle.default._

/**
  * @author Ronny Bräunlich
  */
class TreeServerDataStructure[T](id: DataStructureInstanceId, controlAlgorithm: ControlAlgorithm, val dataTypeName: DataStructureName)(implicit val writer: Writer[T]) extends AbstractServerDataStructure(id, controlAlgorithm) {

  implicit val treeNodeWriter = new ValueTreeNodeWriter[T]()

  override val transformer: OperationTransformer = new TreeTransformer

  override def apply(op: DataStructureOperation): Unit = {
    log.debug(s"Applying operation: $op")
    data = data.applyOperation(op.asInstanceOf[TreeStructureOperation])
  }

  var data: TreeNode = EmptyTreeNode

  override def getDataAsJson: String = {
    if(data == EmptyTreeNode) ""
    else write(data.asInstanceOf[ValueTreeNode])
  }
}

object TreeServerDataStructure {
  def apply[T](id: DataStructureInstanceId, controlAlgorithm: ControlAlgorithm, dataTypeName: DataStructureName)
              (implicit writer: Writer[T]): TreeServerDataStructure[T] = new TreeServerDataStructure(id, controlAlgorithm, dataTypeName)
}
