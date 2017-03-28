package de.tu_berlin.formic.datatype.json

import de.tu_berlin.formic.common.DataStructureInstanceId$
import de.tu_berlin.formic.common.controlalgo.ControlAlgorithm
import de.tu_berlin.formic.common.datatype.{DataTypeName, DataTypeOperation, OperationTransformer}
import de.tu_berlin.formic.common.server.datatype.AbstractServerDataType
import de.tu_berlin.formic.datatype.json.JsonFormicJsonDataTypeProtocol._
import de.tu_berlin.formic.datatype.tree.{TreeNode, TreeStructureOperation}
import upickle.default._
/**
  * @author Ronny Bräunlich
  */
class JsonServerDataType(id: DataStructureInstanceId, controlAlgorithm: ControlAlgorithm, val dataTypeName: DataTypeName) extends AbstractServerDataType(id, controlAlgorithm) {

  var data: TreeNode = ObjectNode(null, List.empty)

  override val transformer: OperationTransformer = new JsonTransformer

  override def apply(op: DataTypeOperation): Unit = {
    log.debug(s"Applying operation: $op")
    data = data.applyOperation(op.asInstanceOf[TreeStructureOperation])
  }

  override def getDataAsJson: String = {
    write(data.asInstanceOf[ObjectNode])
  }
}

object JsonServerDataType {
  def apply[T](id: DataStructureInstanceId, controlAlgorithm: ControlAlgorithm, dataTypeName: DataTypeName): JsonServerDataType = new JsonServerDataType(id, controlAlgorithm, dataTypeName)
}