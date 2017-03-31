package de.tu_berlin.formic.datastructure.json

import de.tu_berlin.formic.common.DataStructureInstanceId
import de.tu_berlin.formic.common.controlalgo.ControlAlgorithm
import de.tu_berlin.formic.common.datastructure.{DataStructureName, DataStructureOperation, OperationTransformer}
import de.tu_berlin.formic.common.server.datastructure.AbstractServerDataStructure
import de.tu_berlin.formic.datatype.tree.{TreeNode, TreeStructureOperation}
import de.tu_berlin.formic.datastructure.json.JsonFormicJsonDataStructureProtocol._
import upickle.default._
/**
  * @author Ronny Bräunlich
  */
class JsonServerDataStructure(id: DataStructureInstanceId, controlAlgorithm: ControlAlgorithm, val dataTypeName: DataStructureName) extends AbstractServerDataStructure(id, controlAlgorithm) {

  var data: TreeNode = ObjectNode(null, List.empty)

  override val transformer: OperationTransformer = new JsonTransformer

  override def apply(op: DataStructureOperation): Unit = {
    log.debug(s"Applying operation: $op")
    data = data.applyOperation(op.asInstanceOf[TreeStructureOperation])
  }

  override def getDataAsJson: String = {
    write(data.asInstanceOf[ObjectNode])
  }
}

object JsonServerDataStructure {
  def apply[T](id: DataStructureInstanceId, controlAlgorithm: ControlAlgorithm, dataTypeName: DataStructureName): JsonServerDataStructure = new JsonServerDataStructure(id, controlAlgorithm, dataTypeName)
}