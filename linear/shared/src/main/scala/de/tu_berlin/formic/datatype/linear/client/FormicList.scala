package de.tu_berlin.formic.datatype.linear.client

import akka.pattern._
import akka.util.Timeout
import de.tu_berlin.formic.common.datatype.FormicDataType.LocalOperationMessage
import de.tu_berlin.formic.common.datatype.client.{ClientDataTypeEvent, DataTypeInitiator}
import de.tu_berlin.formic.common.datatype.{DataTypeName, FormicDataType, OperationContext}
import de.tu_berlin.formic.common.message.{OperationMessage, UpdateRequest, UpdateResponse}
import de.tu_berlin.formic.common.{DataTypeInstanceId, OperationId}
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
abstract class FormicList[T](_callback: (ClientDataTypeEvent) => Unit,
                             initiator: DataTypeInitiator,
                             dataTypeInstanceId: DataTypeInstanceId,
                             dataTypeName: DataTypeName)
                            (implicit val writer: Writer[T], val reader: Reader[T])
  extends FormicDataType(_callback, dataTypeName, dataTypeInstanceId = dataTypeInstanceId, initiator = initiator ) {

  implicit val timeout: Timeout = 1.seconds

  @JSExport
  def add(index: Int, o: T, operationId: OperationId = OperationId()) = {
    val op = LocalOperationMessage(
      OperationMessage(clientId, dataTypeInstanceId, dataTypeName, List(LinearInsertOperation(index, o, operationId, OperationContext(List.empty), clientId)))
    )
    actor ! op
  }

  @JSExport
  def remove(index: Int, operationId: OperationId = OperationId()) = {
    val op = LocalOperationMessage(
      OperationMessage(clientId, dataTypeInstanceId, dataTypeName, List(LinearDeleteOperation(index, operationId, OperationContext(List.empty), clientId)))
    )
    actor ! op
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
