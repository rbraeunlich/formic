package de.tu_berlin.formic.common.datatype.client

import akka.actor.{Actor, ActorLogging, ActorRef, PoisonPill, Props}
import de.tu_berlin.formic.common.controlalgo.ControlAlgorithmClient
import de.tu_berlin.formic.common.datatype.FormicDataType.LocalOperationMessage
import de.tu_berlin.formic.common.datatype._
import de.tu_berlin.formic.common.datatype.client.AbstractClientDataType.{InitialOperation, ReceiveCallback}
import de.tu_berlin.formic.common.datatype.client.CallbackWrapper.Invoke
import de.tu_berlin.formic.common.message.{HistoricOperationRequest, OperationMessage, UpdateRequest, UpdateResponse}
import de.tu_berlin.formic.common.{ClientId, DataTypeInstanceId, OperationId}

/**
  * The data types on the client basically receive only operation messages. Also, they need another
  * type of control algorithm.
  *
  * @author Ronny Bräunlich
  */
abstract class AbstractClientDataType(val id: DataTypeInstanceId, val controlAlgorithm: ControlAlgorithmClient, lastOperationId: Option[OperationId]) extends Actor with ActorLogging {

  val dataTypeName: DataTypeName

  val transformer: OperationTransformer

  val historyBuffer: HistoryBuffer = new HistoryBuffer()
  if (lastOperationId.isDefined) historyBuffer.addOperation(InitialOperation(lastOperationId.get))

  def receive = {
    case ReceiveCallback(callback) =>
      val wrapper = context.actorOf(Props(new CallbackWrapper(callback)))
      context.become(withCallback(wrapper))
  }

  def existsOperationWithDirectContextDependencyMissing(operations: List[DataTypeOperation], historyBuffer: HistoryBuffer): Boolean = {
    operations.
      filterNot(op => isPreviousOperationPresent(op, historyBuffer)).
      exists(op => !operations.exists(
        otherOp => op.operationContext.operations.headOption.contains(otherOp.id)
      )
      )
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
      val duplicatesRemoved = opMsg.operations.filter(op => historyBuffer.findOperation(op.id).isEmpty)
      if (existsOperationWithDirectContextDependencyMissing(duplicatesRemoved, historyBuffer)) {
        sender ! HistoricOperationRequest(null, id, historyBuffer.history.headOption.map(op => op.id).orNull)
      } else {
        duplicatesRemoved.
          reverse.
          filter(op => controlAlgorithm.canBeApplied(op, historyBuffer)).
          foreach(applyOperation)
        callbackWrapper ! Invoke
      }

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


  /**
    * Due to the possibility of being disconnected from the server, it might happen that some operations
    * are missing and operations are being received out of order.
    *
    * @param op            the operation whose OperationContext has to be checked
    * @param historyBuffer the buffer containing all previous operations
    * @return true, if the operation from the operation context is present, false otherwise
    */
  def isPreviousOperationPresent(op: DataTypeOperation, historyBuffer: HistoryBuffer): Boolean = {
    if (op.operationContext.operations.isEmpty) true
    else historyBuffer.findOperation(op.operationContext.operations.head).isDefined
  }

}

object AbstractClientDataType {

  case class ReceiveCallback(callback: () => Unit)

  /**
    * Data types on the client might have been created based on an UpdateResponse.
    * To have a "first" operation in the history, this class exists. It is more like a
    * placeholder for the missing history.
    *
    * @param id the initial operation id
    */
  private case class InitialOperation(id: OperationId) extends DataTypeOperation {
    override val operationContext: OperationContext = OperationContext(List.empty)
    override var clientId: ClientId = _
  }

}