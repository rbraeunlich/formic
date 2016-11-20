package de.tu_berlin.formic.common.datatype

import akka.actor.Actor
import de.tu_berlin.formic.common.DataTypeInstanceId
import de.tu_berlin.formic.common.message.{HistoricOperationRequest, OperationMessage, UpdateRequest, UpdateResponse}

/**
  * @author Ronny Bräunlich
  */
abstract class AbstractDataType(val id: DataTypeInstanceId) extends Actor {

  val historyBuffer: HistoryBuffer = new HistoryBuffer()

  val dataTypeName: DataTypeName

  override def preStart(): Unit = {
    context.system.eventStream.publish(UpdateResponse(id, dataTypeName, getDataAsJson))
  }

  def receive = {
    case opMsg:OperationMessage =>
      opMsg.operations.foreach(op => {
        apply(op)
        historyBuffer.addOperation(op)
      }) //TODO Control Algorithm einbauen
      context.system.eventStream.publish(opMsg)

    case HistoricOperationRequest(clientId, _, since) => sender ! OperationMessage(clientId, id, dataTypeName, historyBuffer.findAllOperationsAfter(since))

    case UpdateRequest(clientId, _) => sender ! UpdateResponse(id, dataTypeName, getDataAsJson)
  }

  def apply(op: DataTypeOperation)

  def getDataAsJson: String
}