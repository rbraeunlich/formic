package de.tu_berlin.formic.common.datatype

import akka.actor.{Actor, ActorLogging}
import de.tu_berlin.formic.common.DataTypeInstanceId
import de.tu_berlin.formic.common.controlalgo.ControlAlgorithmClient
import de.tu_berlin.formic.common.datatype.FormicDataType.LocalOperationMessage
import de.tu_berlin.formic.common.message.OperationMessage

/**
  * The data types on the client basically receive only operation messages. Also, they need another
  * type of control algorithm.
  *
  * @author Ronny Bräunlich
  */
abstract class AbstractClientDataType(val id: DataTypeInstanceId, val controlAlgorithm: ControlAlgorithmClient) extends Actor with ActorLogging {

  val dataTypeName: DataTypeName

  val transformer: OperationTransformer

  val historyBuffer: HistoryBuffer = new HistoryBuffer()

  def receive = {
    case msg: LocalOperationMessage =>
      log.debug(s"Received local operation message $msg")
      //the Client should never generate more than one operation
      val operation = msg.op.operations.head
      if (controlAlgorithm.canLocalOperationBeApplied(operation)) {
        //local operations can be applied immediately by definition
        apply(operation)
        historyBuffer.addOperation(operation)
      }
    case opMsg: OperationMessage =>
      //on the client we expect that the server always sends the operations in correct order
      opMsg.operations.
        reverse.
        foreach(op => {
          if (controlAlgorithm.canBeApplied(op, historyBuffer)) {
            applyOperation(op)
            //the control algorithm might need the duplicates as acks, therefore we filter afterwards
          }
        })
  }

  private def applyOperation(dataTypeOperation: DataTypeOperation) = {
    val transformed = controlAlgorithm.transform(dataTypeOperation, historyBuffer, transformer)
    apply(transformed)
    historyBuffer.addOperation(transformed)
  }

  def apply(op: DataTypeOperation)

  def getDataAsJson: String

}
