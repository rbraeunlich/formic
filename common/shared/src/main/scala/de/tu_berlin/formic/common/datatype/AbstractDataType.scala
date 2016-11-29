package de.tu_berlin.formic.common.datatype

import akka.actor.Actor
import de.tu_berlin.formic.common.DataTypeInstanceId
import de.tu_berlin.formic.common.controlalgo.ControlAlgorithm
import de.tu_berlin.formic.common.datatype.AbstractDataType.GetHistory
import de.tu_berlin.formic.common.message.{HistoricOperationRequest, OperationMessage, UpdateRequest, UpdateResponse}
/**
  * @author Ronny Bräunlich
  */
abstract class AbstractDataType(val id: DataTypeInstanceId,val controlAlgorithm: ControlAlgorithm) extends Actor {

  val historyBuffer: HistoryBuffer = new HistoryBuffer()

  val dataTypeName: DataTypeName

  val transformer: OperationTransformer

  private var privateCausallyNotReadyOperations: Set[DataTypeOperation] = Set.empty

  override def preStart(): Unit = {
    context.system.eventStream.publish(UpdateResponse(id, dataTypeName, getDataAsJson))
  }

  def receive = {
    case opMsg:OperationMessage =>
      //if a client sent several operations, the oldest one will be at the end, therefore we reverse the list here
      opMsg.operations.
        reverse.
        filter(op => historyBuffer.findOperation(op.id).isEmpty). //remove duplicates
        foreach(op => {
        if(controlAlgorithm.canBeApplied(op, historyBuffer)) {
          applyOperation(op)
        } else {
          privateCausallyNotReadyOperations = privateCausallyNotReadyOperations + op
        }
      })
      context.system.eventStream.publish(opMsg)
      applyOperationsThatBecameCausallyReady()

    case HistoricOperationRequest(clientId, _, since) => sender ! OperationMessage(clientId, id, dataTypeName, historyBuffer.findAllOperationsAfter(since))

    case UpdateRequest(clientId, _) => sender ! UpdateResponse(id, dataTypeName, getDataAsJson)

    case GetHistory => sender ! new HistoryBuffer(historyBuffer.history)
  }

  /**
    * Due to new operations that were applied previously stored ones might became causally ready for
    * application. Actually we should create an causal order between all the stored operations but
    * to keep it simple, we'll just recursively call this method until no more operations are ready.
    */
  private def applyOperationsThatBecameCausallyReady(): Unit = {
    val causallyReady = causallyNotReadyOperations.filter(op => controlAlgorithm.canBeApplied(op, historyBuffer))
    if(causallyReady.isEmpty){
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

object AbstractDataType {
  case object GetHistory
}