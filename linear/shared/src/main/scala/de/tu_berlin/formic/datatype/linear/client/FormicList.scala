package de.tu_berlin.formic.datatype.linear.client

import akka.pattern._
import akka.util.Timeout
import de.tu_berlin.formic.common.datatype.FormicDataStructure.LocalOperationMessage
import de.tu_berlin.formic.common.datatype.client.{ClientDataStructureEvent, DataStructureInitiator}
import de.tu_berlin.formic.common.datatype.{DataStructureName, FormicDataStructure, OperationContext}
import de.tu_berlin.formic.common.message.{OperationMessage, UpdateRequest, UpdateResponse}
import de.tu_berlin.formic.common.{DataStructureInstanceId, OperationId}
import de.tu_berlin.formic.datatype.linear.{LinearDeleteOperation, LinearInsertOperation}
import upickle.default._

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js.annotation.{JSExport, JSExportDescendentClasses}

/**
  * @author Ronny Bräunlich
  */
@JSExportDescendentClasses
abstract class FormicList[T](_callback: (ClientDataStructureEvent) => Unit,
                             initiator: DataStructureInitiator,
                             dataTypeInstanceId: DataStructureInstanceId,
                             dataTypeName: DataStructureName)
                            (implicit val writer: Writer[T], val reader: Reader[T])
  extends FormicDataStructure(_callback, dataTypeName, dataStructureInstanceId = dataTypeInstanceId, initiator = initiator ) {

  implicit val timeout: Timeout = 1.seconds

  @JSExport
  def add(index: Int, o: T) = {
    val operationId = OperationId()
    val op = LocalOperationMessage(
      OperationMessage(clientId, dataTypeInstanceId, dataTypeName, List(LinearInsertOperation(index, o, operationId, OperationContext(List.empty), clientId)))
    )
    actor ! op
    operationId
  }

  @JSExport
  def remove(index: Int): OperationId = {
    val operationId = OperationId()
    val op = LocalOperationMessage(
      OperationMessage(clientId, dataTypeInstanceId, dataTypeName, List(LinearDeleteOperation(index, operationId, OperationContext(List.empty), clientId)))
    )
    actor ! op
    operationId
  }

  @JSExport
  def get(index: Int)(implicit ec: ExecutionContext): Future[T] = {
    ask(actor, UpdateRequest(clientId, dataTypeInstanceId)).
      mapTo[UpdateResponse].
      map(rep => {
        rep.data
      }).
      map(data => {
        read[ArrayBuffer[T]](data)
      }).
      map(buffer => buffer(index))
  }

  @JSExport
  def getAll()(implicit ec: ExecutionContext): Future[ArrayBuffer[T]] = {
    val updateRequest = UpdateRequest(clientId, dataTypeInstanceId)
    ask(actor, updateRequest).
      mapTo[UpdateResponse].
      map(rep => rep.data).
      map(data => read[ArrayBuffer[T]](data))
  }
}
