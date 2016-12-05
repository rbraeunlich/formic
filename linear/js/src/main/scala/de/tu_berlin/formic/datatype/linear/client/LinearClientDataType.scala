package de.tu_berlin.formic.datatype.linear.client

import de.tu_berlin.formic.common.DataTypeInstanceId
import de.tu_berlin.formic.common.controlalgo.ControlAlgorithmClient
import de.tu_berlin.formic.common.datatype.client.AbstractClientDataType
import de.tu_berlin.formic.common.datatype.{DataTypeName, DataTypeOperation, OperationTransformer}
import de.tu_berlin.formic.datatype.linear.{LinearDeleteOperation, LinearInsertOperation, LinearTransformer}
import upickle.default._

import scala.collection.mutable.ArrayBuffer

/**
  * @author Ronny Bräunlich
  */
class LinearClientDataType[T](id: DataTypeInstanceId, controlAlgorithmClient: ControlAlgorithmClient, val dataTypeName: DataTypeName, implicit val writer: Writer[T]) extends AbstractClientDataType(id, controlAlgorithmClient) {

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

object LinearClientDataType {

  def apply[T](id: DataTypeInstanceId, controlAlgorithm: ControlAlgorithmClient, dataTypeName: DataTypeName)(implicit writer: Writer[T]): LinearClientDataType[T] = new LinearClientDataType(id, controlAlgorithm, dataTypeName, writer)

}