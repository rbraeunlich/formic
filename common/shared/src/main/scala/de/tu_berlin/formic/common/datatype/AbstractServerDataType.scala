package de.tu_berlin.formic.common.datatype

import akka.actor.{Actor, ActorLogging}
import de.tu_berlin.formic.common.DataTypeInstanceId
import de.tu_berlin.formic.common.controlalgo.ControlAlgorithm
import de.tu_berlin.formic.common.message.{HistoricOperationRequest, OperationMessage, UpdateRequest, UpdateResponse}

/**
  * @author Ronny Bräunlich
  */
abstract class AbstractServerDataType(val id: DataTypeInstanceId, val controlAlgorithm: ControlAlgorithm) extends Actor with ActorLogging {

  val historyBuffer: HistoryBuffer = new HistoryBuffer()

  val dataTypeName: DataTypeName

  val transformer: OperationTransformer

  private var privateCausallyNotReadyOperations: Set[DataTypeOperation] = Set.empty

  override def preStart(): Unit = {
    context.system.eventStream.publish(UpdateResponse(id, dataTypeName, getDataAsJson, Option.empty))
  }

  def receive = {
    case opMsg: OperationMessage =>
      log.debug(s"DataType $id received OperationMessage: $opMsg")
      //if a client sent several operations, the oldest one will be at the end, therefore we reverse the list here
      val duplicatesRemoved = opMsg.operations.reverse.filter(op => historyBuffer.findOperation(op.id).isEmpty)
      val (applicable, nonApplicable) = duplicatesRemoved.partition(controlAlgorithm.canBeApplied(_, historyBuffer))
      val transformed = applicable.map(controlAlgorithm.transform(_, historyBuffer, transformer))
      transformed.
        foreach(op => {
          apply(op)
          historyBuffer.addOperation(op)
          }
        )

      privateCausallyNotReadyOperations ++= nonApplicable

      context.system.eventStream.publish(OperationMessage(opMsg.clientId, opMsg.dataTypeInstanceId, opMsg.dataType, transformed))
      applyOperationsThatBecameCausallyReady()

    case hist: HistoricOperationRequest =>
      log.debug(s"DataType $id received HistoricOperationRequest: $hist")
      sender ! OperationMessage(hist.clientId, id, dataTypeName, historyBuffer.findAllOperationsAfter(hist.sinceId))

    case upd: UpdateRequest =>
      log.debug(s"DataType $id received UpdateRequest: $upd")
      sender ! UpdateResponse(id, dataTypeName, getDataAsJson, historyBuffer.history.headOption.map(op => op.id))
  }

  /**
    * Due to new operations that were applied previously stored ones might became causally ready for
    * application. Actually we should create an causal order between all the stored operations but
    * to keep it simple, we'll just recursively call this method until no more operations are ready.
    */
  private def applyOperationsThatBecameCausallyReady(): Unit = {
    val causallyReady = causallyNotReadyOperations.filter(op => controlAlgorithm.canBeApplied(op, historyBuffer))
    if (causallyReady.isEmpty) {
      return
    }
    privateCausallyNotReadyOperations = privateCausallyNotReadyOperations diff causallyReady
    causallyReady.foreach(op => applyOperation(op))
    applyOperationsThatBecameCausallyReady()
  }

  private def applyOperation(dataTypeOperation: DataTypeOperation) = {
    val transformed = controlAlgorithm.transform(dataTypeOperation, historyBuffer, transformer)
    apply(transformed)
    historyBuffer.addOperation(transformed)
  }

  def apply(op: DataTypeOperation)

  def getDataAsJson: String

  def causallyNotReadyOperations = privateCausallyNotReadyOperations
}