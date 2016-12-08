package de.tu_berlin.formic.common.datatype.client

import akka.actor.{Actor, ActorLogging, ActorRef, PoisonPill, Props}
import de.tu_berlin.formic.common.DataTypeInstanceId
import de.tu_berlin.formic.common.controlalgo.ControlAlgorithmClient
import de.tu_berlin.formic.common.datatype.FormicDataType.LocalOperationMessage
import de.tu_berlin.formic.common.datatype._
import de.tu_berlin.formic.common.datatype.client.AbstractClientDataType.ReceiveCallback
import de.tu_berlin.formic.common.datatype.client.CallbackWrapper.Invoke
import de.tu_berlin.formic.common.message.{OperationMessage, UpdateRequest, UpdateResponse}

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
    case ReceiveCallback(callback) =>
      val wrapper = context.actorOf(Props(new CallbackWrapper(callback)))
      context.become(withCallback(wrapper))
  }

  def withCallback(callbackWrapper: ActorRef): Receive = {
    case msg: LocalOperationMessage =>
      log.debug(s"DataType $id received local operation message $msg")
      //the Client should never generate more than one operation
      val operation = msg.op.operations.head
      val clonedOperation = cloneOperationWithNewContext(
        operation,
        OperationContext(historyBuffer.history.headOption.map(op => op.id).toList)
      )
      if (controlAlgorithm.canLocalOperationBeApplied(clonedOperation)) {
        //local operations can be applied immediately by definition
        apply(operation)
        historyBuffer.addOperation(clonedOperation)
      }
      callbackWrapper ! Invoke

    case opMsg: OperationMessage =>
      log.debug(s"DataType $id received operation message $opMsg")
      //on the client we expect that the server always sends the operations in correct order
      opMsg.operations.
        reverse.
        foreach(op => {
          if (controlAlgorithm.canBeApplied(op, historyBuffer)) {
            applyOperation(op)
            //the control algorithm might need the duplicates as acks, therefore we filter afterwards
          }
        })
      callbackWrapper ! Invoke

    case req: UpdateRequest =>
      //this is only called locally from the wrappers
      log.debug(s"DataType $id received UpdateRequest: $req")
      sender ! UpdateResponse(id, dataTypeName, getDataAsJson, historyBuffer.history.headOption.map(op => op.id))

    case ReceiveCallback(callback) =>
      val newWrapper = context.actorOf(Props(new CallbackWrapper(callback)))
      callbackWrapper ! PoisonPill
      context.become(withCallback(newWrapper))
  }

  private def applyOperation(dataTypeOperation: DataTypeOperation) = {
    val transformed = controlAlgorithm.transform(dataTypeOperation, historyBuffer, transformer)
    apply(transformed)
    historyBuffer.addOperation(transformed)
  }

  def apply(op: DataTypeOperation)

  /**
    * An operation context shall not be modifiable. But since a wrapper cannot directly access
    * the history, the client data type has to change the operation context. Using a GetHistory message does
    * not solve this problem. E.g.<p>
    * op1 -> GetHistory -> Answer<br/>
    * op2 -> GetHistory -> Answer<br/>
    * op1 -> apply<br/>
    * op2 -> apply<br/>
    * </p>
    * then op2 would not have op1 as operation context, although it follows op1.
    * Therefore, for <b>local</b> operations, the data type needs to set the operation context, or in
    * this case clone the operation with a new one.
    */
  def cloneOperationWithNewContext(op: DataTypeOperation, context: OperationContext): DataTypeOperation

  def getDataAsJson: String

}

object AbstractClientDataType {

  case class ReceiveCallback(callback: () => Unit)

}