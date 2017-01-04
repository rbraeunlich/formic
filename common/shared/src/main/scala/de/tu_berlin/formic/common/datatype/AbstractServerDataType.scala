package de.tu_berlin.formic.common.datatype

import akka.actor.ActorLogging
import akka.persistence.{PersistentActor, RecoveryCompleted}
import de.tu_berlin.formic.common.controlalgo.ControlAlgorithm
import de.tu_berlin.formic.common.datatype.AbstractServerDataType.{GetHistory, HistoricOperationsAnswer}
import de.tu_berlin.formic.common.message.{HistoricOperationRequest, OperationMessage, UpdateRequest, UpdateResponse}
import de.tu_berlin.formic.common.DataTypeInstanceId
/**
  * @author Ronny Bräunlich
  */
abstract class AbstractServerDataType(val id: DataTypeInstanceId, val controlAlgorithm: ControlAlgorithm) extends PersistentActor with ActorLogging {

  def persistenceId = id.id

  val historyBuffer: HistoryBuffer = new HistoryBuffer()

  val dataTypeName: DataTypeName

  val transformer: OperationTransformer

  override def preStart(): Unit = {
    context.system.eventStream.publish(UpdateResponse(id, dataTypeName, getDataAsJson, Option.empty))
  }

  val receiveCommand: Receive = {
    case opMsg: OperationMessage =>
      log.debug(s"DataType $id received OperationMessage: $opMsg")
      //if a client sent several operations, the oldest one will be at the end, therefore we reverse the list here
      val duplicatesRemoved = opMsg.operations.reverse.filter(op => historyBuffer.findOperation(op.id).isEmpty)
      val applicable = duplicatesRemoved.filter(controlAlgorithm.canBeApplied(_, historyBuffer))
      //FIXME this is actually not so good because apply() within a subclass could crash because of e.g. a wrong index
      persistAll(applicable)(applyOperation)

    case hist: HistoricOperationRequest =>
      log.debug(s"DataType $id received HistoricOperationRequest: $hist")
      sender !  HistoricOperationsAnswer(OperationMessage(hist.clientId, id, dataTypeName, historyBuffer.findAllOperationsAfter(hist.sinceId)))

    case upd: UpdateRequest =>
      log.debug(s"DataType $id received UpdateRequest: $upd")
      sender ! UpdateResponse(id, dataTypeName, getDataAsJson, historyBuffer.history.headOption.map(op => op.id))

    case GetHistory => sender ! new HistoryBuffer(historyBuffer.history)
  }

  val receiveRecover: Receive = {
    case operation: DataTypeOperation => applyOperation(operation)
    case RecoveryCompleted => log.info(s"Data type ${id.id} $dataTypeName recovered")
  }

  private def applyOperation(dataTypeOperation: DataTypeOperation) = {
    val transformed = controlAlgorithm.transform(dataTypeOperation, historyBuffer, transformer)
    apply(transformed)
    historyBuffer.addOperation(transformed)
    context.system.eventStream.publish(OperationMessage(transformed.clientId, id, dataTypeName, List(transformed)))
  }

  def apply(op: DataTypeOperation)

  def getDataAsJson: String

}

object AbstractServerDataType {
  /**
    * If the answer was an OperationMessage, nobody could distinguish between
    * incoming operations and answers to HistoricOperationRequests. Therfore we need this little
    * wrapper.
    */
  case class HistoricOperationsAnswer(operationMessage: OperationMessage)

  /**
    * For testing purposes only
    */
  case object GetHistory
}