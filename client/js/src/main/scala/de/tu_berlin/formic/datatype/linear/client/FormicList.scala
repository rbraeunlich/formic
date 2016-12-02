package de.tu_berlin.formic.datatype.linear.client

import akka.pattern._
import akka.util.Timeout
import de.tu_berlin.formic.client.FormicSystem
import de.tu_berlin.formic.client.datatype.DataTypeInitiator
import de.tu_berlin.formic.common.datatype.AbstractServerDataType.GetHistory
import de.tu_berlin.formic.common.datatype.{DataTypeName, FormicDataType, HistoryBuffer, OperationContext}
import de.tu_berlin.formic.common.message.{CreateRequest, OperationMessage, UpdateRequest, UpdateResponse}
import de.tu_berlin.formic.common.{DataTypeInstanceId, OperationId}
import de.tu_berlin.formic.datatype.linear.{LinearServerDataType, LinearDeleteOperation, LinearInsertOperation}

import scala.concurrent.duration._
import scala.scalajs.js.annotation.JSExport
import upickle.default._

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.Future

/**
  * @author Ronny Bräunlich
  */
@JSExport
class FormicList[T](var callback: () => Unit, initiator: DataTypeInitiator)(implicit val writer: Writer[T], val reader: Reader[T]) extends FormicDataType {

  val dataTypeInstanceId = DataTypeInstanceId()

  override val dataTypeName: DataTypeName = LinearServerDataType.dataTypeName

  implicit val timeout: Timeout = 1.seconds

  initiator.initDataType(this)
  connection ! CreateRequest(null, dataTypeInstanceId, dataTypeName)

  @JSExport
  def add(index: Int, o: T) = {
    import scala.concurrent.ExecutionContext.Implicits.global
    val operation = ask(actor, GetHistory).
      mapTo[HistoryBuffer].
      map(buffer => buffer.history).
      map(history => history.map(op => op.id)).
      map(operations =>
        OperationMessage(null, dataTypeInstanceId, dataTypeName, List(LinearInsertOperation(index, write(o), OperationId(), OperationContext(operations), null)))
      )
    pipe(operation) to actor
  }

  @JSExport
  def remove(index: Int) = {
    import scala.concurrent.ExecutionContext.Implicits.global
    val operation = ask(actor, GetHistory).
      mapTo[HistoryBuffer].
      map(buffer => buffer.history).
      map(history => history.map(op => op.id)).
      map(operations =>
        OperationMessage(null, dataTypeInstanceId, dataTypeName, List(LinearDeleteOperation(index, OperationId(), OperationContext(operations), null)))
      )
    pipe(operation) to actor
  }

  @JSExport
  def get(index: Int): Future[T] = {
    import scala.concurrent.ExecutionContext.Implicits.global
    ask(actor, UpdateRequest(_,_)).
      mapTo[UpdateResponse].
      map(rep => rep.data).
      map(data => read[ArrayBuffer[String]](data)).
      map(buffer => buffer(index)).
      map(element => read[T](element))
  }

  @JSExport
  def getAll: Future[ArrayBuffer[T]] = {
    import scala.concurrent.ExecutionContext.Implicits.global
    ask(actor, UpdateRequest(_,_)).
      mapTo[UpdateResponse].
      map(rep => rep.data).
      map(data => read[ArrayBuffer[String]](data)).
      map(buffer => buffer.map(s => read[T](s)))
  }
}
