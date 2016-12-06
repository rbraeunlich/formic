package de.tu_berlin.formic.datatype.linear.client

import akka.pattern._
import akka.util.Timeout
import de.tu_berlin.formic.common.datatype.FormicDataType.LocalOperationMessage
import de.tu_berlin.formic.common.datatype.client.AbstractClientDataType.{GetHistory, ReceiveCallback}
import de.tu_berlin.formic.common.datatype.client.DataTypeInitiator
import de.tu_berlin.formic.common.datatype.{DataTypeName, FormicDataType, HistoryBuffer, OperationContext}
import de.tu_berlin.formic.common.message.{OperationMessage, UpdateRequest, UpdateResponse}
import de.tu_berlin.formic.common.{DataTypeInstanceId, OperationId}
import de.tu_berlin.formic.datatype.linear.{LinearDeleteOperation, LinearInsertOperation}
import upickle.default._

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.scalajs.js.annotation.{JSExport, JSExportDescendentClasses}

/**
  * @author Ronny Bräunlich
  */
@JSExportDescendentClasses
abstract class FormicList[T](private var _callback: () => Unit, initiator: DataTypeInitiator, val dataTypeInstanceId: DataTypeInstanceId, val dataTypeName: DataTypeName)(implicit val writer: Writer[T], val reader: Reader[T]) extends FormicDataType {

  implicit val timeout: Timeout = 1.seconds

  override def callback: () => Unit = _callback

  override def callback_= (newCallback: () => Unit){
    _callback = newCallback
    actor ! ReceiveCallback(newCallback)
  }


  //TODO find a better way to distinguish remote and local instantiations
  if (initiator != null) initiator.initDataType(this)

  @JSExport
  def add(index: Int, o: T) = {
    import scala.concurrent.ExecutionContext.Implicits.global
        LocalOperationMessage(
          OperationMessage(null, dataTypeInstanceId, dataTypeName, List(LinearInsertOperation(index, o, OperationId(), OperationContext(List.empty), null)))
        )
  }

  @JSExport
  def remove(index: Int) = {
    import scala.concurrent.ExecutionContext.Implicits.global
    val operation = ask(actor, GetHistory).
      mapTo[HistoryBuffer].
      map(buffer => buffer.history.headOption).
      map {
        case Some(entry) => List(entry.id)
        case None => List.empty
      }.
      map(operations =>
        LocalOperationMessage(
          OperationMessage(null, dataTypeInstanceId, dataTypeName, List(LinearDeleteOperation(index, OperationId(), OperationContext(operations), null)))
        )
      )
    pipe(operation) to actor
  }

  @JSExport
  def get(index: Int): Future[T] = {
    import scala.concurrent.ExecutionContext.Implicits.global
    ask(actor, UpdateRequest(_, _)).
      mapTo[UpdateResponse].
      map(rep => rep.data).
      map(data => read[ArrayBuffer[T]](data)).
      map(buffer => buffer(index))
  }

  @JSExport
  def getAll: Future[ArrayBuffer[T]] = {
    import scala.concurrent.ExecutionContext.Implicits.global
    val updateRequest = UpdateRequest(null, dataTypeInstanceId)
    ask(actor, updateRequest).
      mapTo[UpdateResponse].
      map(rep => rep.data).
      map(data => read[ArrayBuffer[T]](data))
  }
}
