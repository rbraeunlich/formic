package de.tu_berlin.formic.datatype.linear.client

import de.tu_berlin.formic.common.{DataTypeInstanceId, OperationId}
import de.tu_berlin.formic.common.controlalgo.ControlAlgorithmClient
import de.tu_berlin.formic.common.datatype.client.AbstractClientDataType
import de.tu_berlin.formic.common.datatype.{DataTypeName, DataTypeOperation, OperationContext, OperationTransformer}
import de.tu_berlin.formic.datatype.linear.{LinearDeleteOperation, LinearInsertOperation, LinearTransformer}
import upickle.default._

import scala.collection.mutable.ArrayBuffer

/**
  * @author Ronny Bräunlich
  */
class LinearClientDataType[T](id: DataTypeInstanceId,
                              controlAlgorithmClient: ControlAlgorithmClient,
                              val dataTypeName: DataTypeName,
                              val initialData: Option[String],
                              lastOperationId: Option[OperationId])
                             (implicit val writer: Writer[T], val reader: Reader[T])
  extends AbstractClientDataType(id, controlAlgorithmClient, lastOperationId) {


  override val transformer: OperationTransformer = LinearTransformer

  private val privateData = initialData.map(s => read[ArrayBuffer[T]](s)).getOrElse(new ArrayBuffer[T]())

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

  override def cloneOperationWithNewContext(op: DataTypeOperation, context: OperationContext): DataTypeOperation = {
    op match {
      case in:LinearInsertOperation => LinearInsertOperation(in.index, in.o, in.id, context, in.clientId)
      case del:LinearDeleteOperation => LinearDeleteOperation(del.index, del.id, context, del.clientId)
    }
  }
}

object LinearClientDataType {

  def apply[T](id: DataTypeInstanceId, controlAlgorithm: ControlAlgorithmClient, dataTypeName: DataTypeName, initialData: Option[String],lastOperationId: Option[OperationId])(implicit writer: Writer[T], reader: Reader[T]): LinearClientDataType[T] =
    new LinearClientDataType(id, controlAlgorithm, dataTypeName, initialData, lastOperationId)

}