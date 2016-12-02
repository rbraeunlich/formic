package de.tu_berlin.formic.datatype.linear

import de.tu_berlin.formic.common.DataTypeInstanceId
import de.tu_berlin.formic.common.controlalgo.ControlAlgorithm
import de.tu_berlin.formic.common.datatype.{AbstractServerDataType, DataTypeName, DataTypeOperation, OperationTransformer}
import upickle.default._

import scala.collection.mutable.ArrayBuffer

/**
  * A data type that represents a structure that can be addressed by a linear address space.
  *
  * @author Ronny Bräunlich
  */
class LinearServerDataType[T](id: DataTypeInstanceId, controlAlgorithm: ControlAlgorithm, implicit val writer: Writer[T]) extends AbstractServerDataType(id, controlAlgorithm) {

  override val dataTypeName: DataTypeName = LinearServerDataType.dataTypeName

  override val transformer: OperationTransformer = LinearTransformer

  private val privateData = ArrayBuffer[T]()

  def data = privateData

  override def apply(op: DataTypeOperation): Unit = {
    log.debug(s"Applying operation: $op")
    op match {
      case LinearInsertOperation(index, o, _, _, _) => privateData.insert(index, o.asInstanceOf[T])
      case LinearDeleteOperation(index, _, _, _) => privateData.remove(index)
    }

  }

  override def getDataAsJson: String = {
    write(data)
  }
}

object LinearServerDataType {

  val dataTypeName = DataTypeName("linear")

  def apply[T](id: DataTypeInstanceId, controlAlgorithm: ControlAlgorithm)(implicit writer: Writer[T]): LinearServerDataType[T] = new LinearServerDataType(id, controlAlgorithm, writer)
}
