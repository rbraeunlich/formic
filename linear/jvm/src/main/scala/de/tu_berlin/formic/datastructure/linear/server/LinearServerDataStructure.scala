package de.tu_berlin.formic.datastructure.linear.server

import de.tu_berlin.formic.common.DataStructureInstanceId
import de.tu_berlin.formic.common.controlalgo.ControlAlgorithm
import de.tu_berlin.formic.common.datastructure.{DataStructureName, DataStructureOperation, OperationTransformer}
import de.tu_berlin.formic.common.server.datastructure.AbstractServerDataStructure
import de.tu_berlin.formic.datastructure.linear.{LinearDeleteOperation, LinearInsertOperation, LinearNoOperation, LinearTransformer}
import upickle.default._

import scala.collection.mutable.ArrayBuffer

/**
  * A data type that represents a structure that can be addressed by a linear address space.
  *
  * @author Ronny Bräunlich
  */
class LinearServerDataStructure[T](id: DataStructureInstanceId, controlAlgorithm: ControlAlgorithm, val dataTypeName: DataStructureName)(implicit val writer: Writer[T]) extends AbstractServerDataStructure(id, controlAlgorithm) {

  override val transformer: OperationTransformer = LinearTransformer

  private val privateData = ArrayBuffer[T]()

  def data = privateData

  override def apply(op: DataStructureOperation): Unit = {
    log.debug(s"Applying operation: $op")
    op match {
      case LinearInsertOperation(index, o, _, _, _) => privateData.insert(index, o.asInstanceOf[T])
      case LinearDeleteOperation(index, _, _, _) => privateData.remove(index)
      case LinearNoOperation(_, _, _) => //do nothing
    }
  }

  override def getDataAsJson: String = {
    write(data)
  }
}

object LinearServerDataStructure {

  def apply[T](id: DataStructureInstanceId, controlAlgorithm: ControlAlgorithm, dataTypeName: DataStructureName)(implicit writer: Writer[T]): LinearServerDataStructure[T] = new LinearServerDataStructure(id, controlAlgorithm, dataTypeName)
}
