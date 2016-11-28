package de.tu_berlin.formic.datatype.linear.client

import akka.pattern._
import akka.util.Timeout
import de.tu_berlin.formic.client.FormicSystem
import de.tu_berlin.formic.common.datatype.AbstractDataType.GetHistory
import de.tu_berlin.formic.common.datatype.{DataTypeName, FormicDataType, HistoryBuffer, OperationContext}
import de.tu_berlin.formic.common.message.{CreateRequest, OperationMessage}
import de.tu_berlin.formic.common.{DataTypeInstanceId, OperationId}
import de.tu_berlin.formic.datatype.linear.{LinearDataType, LinearDeleteOperation, LinearInsertOperation}

import scala.scalajs.js.annotation.JSExport
import scala.concurrent.duration._

/**
  * @author Ronny Bräunlich
  */
@JSExport
class FormicString(var callback: () => Unit, formicSystem: FormicSystem) extends FormicDataType {

  val dataTypeInstanceId = DataTypeInstanceId()

  override val dataTypeName: DataTypeName = LinearDataType.dataTypeName

  implicit val timeout: Timeout = 1.seconds

  formicSystem.initDataType(this)
  connection ! CreateRequest(null, dataTypeInstanceId, dataTypeName)

  @JSExport
  def addCharacter(index: Int, character: Character) = {
    import scala.concurrent.ExecutionContext.Implicits.global
    val operation = ask(actor, GetHistory).
      mapTo[HistoryBuffer].
      map(buffer => buffer.history).
      map(history => history.map(op => op.id)).
      map(operations =>
        OperationMessage(null, dataTypeInstanceId, dataTypeName, List(LinearInsertOperation(index, character, OperationId(), OperationContext(operations), null)))
      )
    pipe(operation) to actor
  }

  @JSExport
  def deleteCharacter(index: Int) = {
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
}
