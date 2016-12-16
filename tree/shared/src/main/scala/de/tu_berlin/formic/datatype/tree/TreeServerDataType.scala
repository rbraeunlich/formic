package de.tu_berlin.formic.datatype.tree

import de.tu_berlin.formic.common.DataTypeInstanceId
import de.tu_berlin.formic.common.controlalgo.ControlAlgorithm
import de.tu_berlin.formic.common.datatype.{AbstractServerDataType, DataTypeName, DataTypeOperation, OperationTransformer}
import upickle.default._

/**
  * @author Ronny Bräunlich
  */
class TreeServerDataType[T](id: DataTypeInstanceId, controlAlgorithm: ControlAlgorithm, val dataTypeName: DataTypeName)(implicit val writer: Writer[T]) extends AbstractServerDataType(id, controlAlgorithm) {

  implicit val treeNodeWriter = new TreeNodeWriter[T]()

  override val transformer: OperationTransformer = TreeTransformer

  override def apply(op: DataTypeOperation): Unit = {
    if (data != null) data.applyOperation(op.asInstanceOf[TreeStructureOperation])
    else op match {
      case TreeInsertOperation(_, tree, _, _, _) => data = tree
      case _ => //just stay silent
    }
  }

  var data: TreeNode = _

  override def getDataAsJson: String = {
    write(data)
  }
}

object TreeServerDataType {
  def apply[T](id: DataTypeInstanceId, controlAlgorithm: ControlAlgorithm, dataTypeName: DataTypeName)
              (implicit writer: Writer[T]): TreeServerDataType[T] = new TreeServerDataType(id, controlAlgorithm, dataTypeName)
}
