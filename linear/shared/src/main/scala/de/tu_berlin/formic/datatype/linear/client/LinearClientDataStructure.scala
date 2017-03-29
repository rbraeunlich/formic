package de.tu_berlin.formic.datatype.linear.client

import akka.actor.ActorRef
import de.tu_berlin.formic.common.controlalgo.ControlAlgorithmClient
import de.tu_berlin.formic.common.datatype.client.AbstractClientDataStructure
import de.tu_berlin.formic.common.datatype.{DataStructureName, DataTypeOperation, OperationContext, OperationTransformer}
import de.tu_berlin.formic.common.{DataStructureInstanceId, OperationId}
import de.tu_berlin.formic.datatype.linear.{LinearDeleteOperation, LinearInsertOperation, LinearNoOperation, LinearTransformer}
import upickle.default._

import scala.collection.mutable.ArrayBuffer

/**
  * @author Ronny Bräunlich
  */
class LinearClientDataStructure[T](id: DataStructureInstanceId,
                                   controlAlgorithmClient: ControlAlgorithmClient,
                                   val dataTypeName: DataStructureName,
                                   val initialData: Option[String],
                                   lastOperationId: Option[OperationId],
                                   outgoingConnection: ActorRef)
                                  (implicit val writer: Writer[T], val reader: Reader[T])
  extends AbstractClientDataStructure(id, controlAlgorithmClient, lastOperationId, outgoingConnection) {


  override val transformer: OperationTransformer = LinearTransformer

  private val privateData = initialData.map(s => read[ArrayBuffer[T]](s)).getOrElse(new ArrayBuffer[T]())

  def data = privateData

  override def apply(op: DataTypeOperation): Unit = {
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

  override def cloneOperationWithNewContext(op: DataTypeOperation, context: OperationContext): DataTypeOperation = {
    op match {
      case in: LinearInsertOperation => LinearInsertOperation(in.index, in.o, in.id, context, in.clientId)
      case del: LinearDeleteOperation => LinearDeleteOperation(del.index, del.id, context, del.clientId)
    }
  }
}

object LinearClientDataStructure {

  def apply[T](id: DataStructureInstanceId, controlAlgorithm: ControlAlgorithmClient, dataTypeName: DataStructureName, initialData: Option[String], lastOperationId: Option[OperationId], outgoingConnection: ActorRef)(implicit writer: Writer[T], reader: Reader[T]): LinearClientDataStructure[T] =
    new LinearClientDataStructure(id, controlAlgorithm, dataTypeName, initialData, lastOperationId, outgoingConnection)

}