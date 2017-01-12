package de.tu_berlin.formic.common.datatype.client

import akka.actor.{Actor, ActorLogging, ActorRef, PoisonPill, Props}
import de.tu_berlin.formic.common.controlalgo.ControlAlgorithmClient
import de.tu_berlin.formic.common.datatype.FormicDataType.LocalOperationMessage
import de.tu_berlin.formic.common.datatype._
import de.tu_berlin.formic.common.datatype.client.AbstractClientDataType._
import de.tu_berlin.formic.common.datatype.client.CallbackWrapper.Invoke
import de.tu_berlin.formic.common.message._
import de.tu_berlin.formic.common.{ClientId, DataTypeInstanceId, OperationId}

/**
  * The data types on the client basically receive only operation messages. Also, they need another
  * type of control algorithm.
  *
  * @author Ronny Bräunlich
  */
abstract class AbstractClientDataType(val id: DataTypeInstanceId,
                                      val controlAlgorithm: ControlAlgorithmClient,
                                      val lastOperationId: Option[OperationId],
                                      val outgoingConnection: ActorRef) extends Actor with ActorLogging {

  val dataTypeName: DataTypeName

  val transformer: OperationTransformer

  val historyBuffer: HistoryBuffer = new HistoryBuffer()

  def receive = {
    case ReceiveCallback(callback) =>
      val wrapper = context.actorOf(Props(new CallbackWrapper(callback)))
      context.become(unacknowledged(wrapper))
    case RemoteInstantiation =>
      context.become(acknowledgedWithoutCallback)
  }

  /**
    * Data types that are created on the client because of an UpdateRequest are already acknowledged,
    * therefore, this intermediate state is needed.
    */
  def acknowledgedWithoutCallback: Receive = {
    case ReceiveCallback(callback) =>
      val wrapper = context.actorOf(Props(new CallbackWrapper(callback)))
      context.become(acknowledged(wrapper))
  }

  /**
    * A data type that has not been acknowledged by the server yet, cannot send its operations to
    * the server.
    */
  def unacknowledged(callbackWrapper: ActorRef): Receive = {
    case msg: LocalOperationMessage =>
      log.debug(s"DataType $id received local operation message $msg")
      //the Client should never generate more than one operation
      val operation = msg.op.operations.head
      val clonedOperation = cloneOperationWithNewContext(
        operation,
      if(historyBuffer.history.isEmpty && lastOperationId.isDefined){
        OperationContext(List(lastOperationId.get))
      }
      else OperationContext(historyBuffer.history.headOption.map(op => op.id).toList)
      )
      apply(operation)
      historyBuffer.addOperation(clonedOperation)
      callbackWrapper ! Invoke

    case rep: CreateResponse =>
      log.debug(s"DataType $id received CreateResponse $rep")
      //we have to inform the control algorithm about the buffered operations
      historyBuffer.history.reverse.foreach(controlAlgorithm.canLocalOperationBeApplied)
      //TODO WaveOT will actually send the same operations again here, gotta find a better way
      outgoingConnection ! OperationMessage(null, id, dataTypeName, historyBuffer.history)
      callbackWrapper ! Invoke
      context.become(acknowledged(callbackWrapper))

    case ReceiveCallback(callback) =>
      val newWrapper = context.actorOf(Props(new CallbackWrapper(callback)))
      callbackWrapper ! PoisonPill
      context.become(unacknowledged(newWrapper))


    case req: UpdateRequest =>
      //this is only called locally from the wrappers
      log.debug(s"DataType $id received UpdateRequest: $req")
      sender ! UpdateResponse(id, dataTypeName, getDataAsJson, historyBuffer.history.headOption.map(op => op.id))
  }

  def acknowledged(callbackWrapper: ActorRef): Receive = {
    case msg: LocalOperationMessage =>
      log.debug(s"DataType $id received local operation message $msg")
      //the Client should never generate more than one operation
      val operation = msg.op.operations.head
      //setting the new context MUST happen before calling canLocalOperationBeApplied
      val clonedOperation = cloneOperationWithNewContext(
        operation,
        //a data type that results from a remote instantiation never told the control algo about
        //the initial operation, therefore we have to distinguish that here
        if(historyBuffer.history.isEmpty && lastOperationId.isDefined){
          OperationContext(List(lastOperationId.get))
        }
        else controlAlgorithm.currentOperationContext
      )
      if (controlAlgorithm.canLocalOperationBeApplied(clonedOperation)) {
        //local operations can be applied immediately by definition
        apply(operation)
        historyBuffer.addOperation(clonedOperation)
      }
      callbackWrapper ! Invoke

    case opMsg: OperationMessage =>
      log.debug(s"DataType $id received operation message $opMsg")
      //we do not filter here for duplicates because that would remove acknowledgements
      //the control algorithm has to do that
      if (existsOperationWithDirectContextDependencyMissing(opMsg.operations, historyBuffer)) {
        sender ! HistoricOperationRequest(null, id, historyBuffer.history.headOption.map(op => op.id).orNull)
      } else {
        opMsg.operations.
          reverse.
          filter(op => controlAlgorithm.canBeApplied(op, historyBuffer)).
          foreach(applyOperation)
        callbackWrapper ! Invoke
      }

    case ReceiveCallback(callback) =>
      val newWrapper = context.actorOf(Props(new CallbackWrapper(callback)))
      callbackWrapper ! PoisonPill
      context.become(acknowledged(newWrapper))

    case req: UpdateRequest =>
      //this is only called locally from the wrappers
      log.debug(s"DataType $id received UpdateRequest: $req")
      sender ! UpdateResponse(id, dataTypeName, getDataAsJson, historyBuffer.history.headOption.map(op => op.id))
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

  /**
    * Since the compressed OperationContext only contains a single operation, we have to check
    * if that operation is present, either in the other operations or in the history. If not, the
    * operation cannot be applied.
    */
  def existsOperationWithDirectContextDependencyMissing(operations: List[DataTypeOperation], historyBuffer: HistoryBuffer): Boolean = {
    operations.
      filterNot(op => isPreviousOperationPresent(op, historyBuffer)).
      exists(op => !operations.exists(
        otherOp => op.operationContext.operations.headOption.contains(otherOp.id))
      )
  }
}

object AbstractClientDataType {

  /**
    * This message object is needed to give the data type a hint, if it has to wait for an acknowledgement
    * or not after being instantiated.
    */
  case object RemoteInstantiation

  case class ReceiveCallback(callback: () => Unit)

}