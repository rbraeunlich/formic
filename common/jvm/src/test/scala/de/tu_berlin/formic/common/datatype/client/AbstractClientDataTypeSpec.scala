package de.tu_berlin.formic.common.datatype.client

import java.util.concurrent.{CountDownLatch, TimeUnit}

import akka.actor.{ActorRef, ActorSystem, Props, Terminated}
import akka.testkit.{ImplicitSender, TestActorRef, TestKit, TestProbe}
import de.tu_berlin.formic.common.controlalgo.{ControlAlgorithmClient, WaveOTClient}
import de.tu_berlin.formic.common.datatype.FormicDataStructure.LocalOperationMessage
import de.tu_berlin.formic.common.datatype._
import de.tu_berlin.formic.common.datatype.client.AbstractClientDataStructure.{ReceiveCallback, RemoteInstantiation}
import de.tu_berlin.formic.common.message._
import de.tu_berlin.formic.common.{ClientId, DataStructureInstanceId, OperationId}
import org.scalatest.Assertions._
import org.scalatest.{Matchers, WordSpecLike}

import scala.concurrent.duration._

/**
  * @author Ronny Bräunlich
  */
class AbstractClientDataTypeSpec extends TestKit(ActorSystem("AbstractClientDataTypeSpec"))
  with WordSpecLike
  with ImplicitSender
  with StopSystemAfterAll
  with Matchers {

  "AbstractClientDataType" must {

    "ignore messages until callback is set" in {
      val dataType: TestActorRef[AbstractClientDataTypeTestClientDataStructure] = TestActorRef(
        Props(new AbstractClientDataTypeTestClientDataStructure(DataStructureInstanceId(), new AbstractClientDataTypeSpecControlAlgorithmClient, outgoingConnection = TestProbe().ref)))
      val operationMessage = OperationMessage(
        ClientId(),
        DataStructureInstanceId(),
        AbstractClientDataTypeSpec.dataTypeName,
        List(
          AbstractClientDataTypeSpecTestOperation(OperationId(), OperationContext(List.empty), ClientId(), "")
        )
      )

      dataType ! LocalOperationMessage(operationMessage)
      expectNoMsg()
      dataType ! operationMessage
      expectNoMsg()
      dataType ! UpdateRequest(ClientId(), DataStructureInstanceId())
      expectNoMsg()

      dataType.underlyingActor.historyBuffer.history shouldBe empty
    }

    "apply received local operations immediately from an local operation message" in {
      val dataTypeInstanceId = DataStructureInstanceId()
      val data = "{foo}"
      val dataType: TestActorRef[AbstractClientDataTypeTestClientDataStructure] = TestActorRef(
        Props(new AbstractClientDataTypeTestClientDataStructure(dataTypeInstanceId, new AbstractClientDataTypeSpecControlAlgorithmClient, outgoingConnection = TestProbe().ref)))
      dataType ! ReceiveCallback((_) => {})
      val operationMessage = OperationMessage(
        ClientId(),
        dataTypeInstanceId,
        AbstractClientDataTypeSpec.dataTypeName,
        List(
          AbstractClientDataTypeSpecTestOperation(OperationId(), OperationContext(List.empty), ClientId(), data)
        )
      )

      dataType ! LocalOperationMessage(operationMessage)

      dataType.underlyingActor.data should equal(data)
    }

    "not transform local operations" in {
      val dataTypeInstanceId = DataStructureInstanceId()
      val data = "{foo}"
      val controlAlgo = new AbstractClientDataTypeSpecControlAlgorithmClient
      val dataType: TestActorRef[AbstractClientDataTypeTestClientDataStructure] = TestActorRef(
        Props(new AbstractClientDataTypeTestClientDataStructure(dataTypeInstanceId, controlAlgo, outgoingConnection = TestProbe().ref)))
      dataType ! ReceiveCallback((_) => {})
      val operationMessage = OperationMessage(
        ClientId(),
        dataTypeInstanceId,
        AbstractClientDataTypeSpec.dataTypeName,
        List(
          AbstractClientDataTypeSpecTestOperation(OperationId(), OperationContext(List.empty), ClientId(), data)
        )
      )

      dataType ! LocalOperationMessage(operationMessage)

      controlAlgo.didTransform should equal(false)
    }

    "add local operations to the history buffer" in {
      val dataTypeInstanceId = DataStructureInstanceId()
      val data = "{foo}"
      val dataType: TestActorRef[AbstractClientDataTypeTestClientDataStructure] = TestActorRef(
        Props(new AbstractClientDataTypeTestClientDataStructure(dataTypeInstanceId, new AbstractClientDataTypeSpecControlAlgorithmClient, outgoingConnection = TestProbe().ref)))
      dataType ! ReceiveCallback((_) => {})
      val operation = AbstractClientDataTypeSpecTestOperation(OperationId(), OperationContext(List.empty), ClientId(), data)
      val operationMessage = OperationMessage(
        ClientId(),
        dataTypeInstanceId,
        AbstractClientDataTypeSpec.dataTypeName,
        List(operation)
      )

      dataType ! LocalOperationMessage(operationMessage)

      dataType.underlyingActor.historyBuffer.history should contain(operation)
    }

    "update the operation context of local operations when unacknowledged" in {
      val dataTypeInstanceId = DataStructureInstanceId()
      val data = "{foo}"
      val dataType: TestActorRef[AbstractClientDataTypeTestClientDataStructure] = TestActorRef(
        Props(new AbstractClientDataTypeTestClientDataStructure(dataTypeInstanceId, new AbstractClientDataTypeSpecControlAlgorithmClient, outgoingConnection = TestProbe().ref)))
      dataType ! ReceiveCallback((_) => {})
      val operation = AbstractClientDataTypeSpecTestOperation(OperationId(), OperationContext(List.empty), ClientId(), data)
      val operation2 = AbstractClientDataTypeSpecTestOperation(OperationId(), OperationContext(List.empty), ClientId(), data)
      val operationMessage = OperationMessage(
        ClientId(),
        dataTypeInstanceId,
        AbstractClientDataTypeSpec.dataTypeName,
        List(operation)
      )
      val operationMessage2 = OperationMessage(
        ClientId(),
        dataTypeInstanceId,
        AbstractClientDataTypeSpec.dataTypeName,
        List(operation2)
      )

      dataType ! LocalOperationMessage(operationMessage)
      dataType ! LocalOperationMessage(operationMessage2)

      dataType.underlyingActor.historyBuffer.history.head should equal(AbstractClientDataTypeSpecTestOperation(operation2.id, OperationContext(List(operation.id)), operation2.clientId, operation2.data))
    }

    "update the operation context of local operations when acknowledged using the control algorithm" in {
      val dataTypeInstanceId = DataStructureInstanceId()
      val data = "{foo}"
      val dataType: TestActorRef[AbstractClientDataTypeTestClientDataStructure] = TestActorRef(
        Props(new AbstractClientDataTypeTestClientDataStructure(dataTypeInstanceId, new AbstractClientDataTypeSpecControlAlgorithmClient, outgoingConnection = TestProbe().ref)))
      dataType ! ReceiveCallback((_) => {})
      dataType ! CreateResponse(dataTypeInstanceId)
      val operation = AbstractClientDataTypeSpecTestOperation(OperationId(), OperationContext(List.empty), ClientId(), data)
      val operation2 = AbstractClientDataTypeSpecTestOperation(OperationId(), OperationContext(List.empty), ClientId(), data)
      val operationMessage = OperationMessage(
        ClientId(),
        dataTypeInstanceId,
        AbstractClientDataTypeSpec.dataTypeName,
        List(operation)
      )
      val operationMessage2 = OperationMessage(
        ClientId(),
        dataTypeInstanceId,
        AbstractClientDataTypeSpec.dataTypeName,
        List(operation2)
      )

      dataType ! LocalOperationMessage(operationMessage)
      dataType ! LocalOperationMessage(operationMessage2)

      dataType.underlyingActor.historyBuffer.history.head should equal(AbstractClientDataTypeSpecTestOperation(operation2.id, OperationContext(List(operation.id)), operation2.clientId, operation2.data))
    }

    "add remote operations to the history buffer" in {
      val dataTypeInstanceId = DataStructureInstanceId()
      val data = "{foo}"
      val dataType: TestActorRef[AbstractClientDataTypeTestClientDataStructure] = TestActorRef(
        Props(new AbstractClientDataTypeTestClientDataStructure(dataTypeInstanceId, new AbstractClientDataTypeSpecControlAlgorithmClient, outgoingConnection = TestProbe().ref)))
      dataType ! ReceiveCallback((_) => {})
      dataType ! CreateResponse(dataTypeInstanceId)
      val operation = AbstractClientDataTypeSpecTestOperation(OperationId(), OperationContext(List.empty), ClientId(), data)
      val operationMessage = OperationMessage(
        ClientId(),
        dataTypeInstanceId,
        AbstractClientDataTypeSpec.dataTypeName,
        List(operation)
      )

      dataType ! operationMessage

      dataType.underlyingActor.historyBuffer.history should contain(operation)
    }

    "apply received operations from an operation message" in {
      val dataTypeInstanceId = DataStructureInstanceId()
      val dataType: TestActorRef[AbstractClientDataTypeTestClientDataStructure] = TestActorRef(
        Props(new AbstractClientDataTypeTestClientDataStructure(dataTypeInstanceId, new AbstractClientDataTypeSpecControlAlgorithmClient, outgoingConnection = TestProbe().ref)))
      dataType ! ReceiveCallback((_) => {})
      val data = "{foo}"
      val operation = AbstractClientDataTypeSpecTestOperation(OperationId(), OperationContext(List.empty), ClientId(), data)
      val operationMessage = OperationMessage(
        ClientId(),
        dataTypeInstanceId,
        AbstractClientDataTypeSpec.dataTypeName,
        List(operation)
      )

      dataType ! LocalOperationMessage(operationMessage)

      dataType.underlyingActor.data should equal(data)
    }

    "not apply operations when the ControlAlgorithm states they are not ready and store them" in {
      val dataTypeInstanceId = DataStructureInstanceId()
      val dataType: TestActorRef[AbstractClientDataTypeTestClientDataStructure] = TestActorRef(
        Props(new AbstractClientDataTypeTestClientDataStructure(dataTypeInstanceId, new AbstractClientDataTypeSpecControlAlgorithmClient(false), outgoingConnection = TestProbe().ref)))
      dataType ! ReceiveCallback((_) => {})
      dataType ! CreateResponse(dataTypeInstanceId)
      val operation = AbstractClientDataTypeSpecTestOperation(OperationId(), OperationContext(List.empty), ClientId(), "{}")
      val message = OperationMessage(
        ClientId(),
        DataStructureInstanceId(),
        AbstractClientDataTypeSpec.dataTypeName,
        List(operation)
      )
      dataType ! message

      dataType.underlyingActor.historyBuffer.history should not contain operation
    }

    "pass operations to the control algorithm for transformation" in {
      var hasBeenTransformed = false
      val controlAlgo: ControlAlgorithmClient = new ControlAlgorithmClient {
        override def transform(op: DataTypeOperation, history: HistoryBuffer, transformer: OperationTransformer): DataTypeOperation = {
          hasBeenTransformed = true
          op
        }

        override def canBeApplied(op: DataTypeOperation, history: HistoryBuffer): Boolean = {
          true
        }

        override def canLocalOperationBeApplied(op: DataTypeOperation): Boolean = true

        override def currentOperationContext: OperationContext = OperationContext(List.empty) //not important here
      }

      val dataType: TestActorRef[AbstractClientDataTypeTestClientDataStructure] = TestActorRef(
        Props(new AbstractClientDataTypeTestClientDataStructure(DataStructureInstanceId(), controlAlgo, outgoingConnection = TestProbe().ref)))
      dataType ! ReceiveCallback((_) => {})
      dataType ! CreateResponse(DataStructureInstanceId())
      val operation = AbstractClientDataTypeSpecTestOperation(OperationId(), OperationContext(List.empty), ClientId(), "{1}")
      val message = OperationMessage(
        ClientId(),
        DataStructureInstanceId(),
        AbstractClientDataTypeSpec.dataTypeName,
        List(operation)
      )

      dataType ! message

      hasBeenTransformed should be(true)
    }

    "replace a callback when receiving a new one and kill the old one" in {
      val dataType: TestActorRef[AbstractClientDataTypeTestClientDataStructure] = TestActorRef(
        Props(new AbstractClientDataTypeTestClientDataStructure(DataStructureInstanceId(), new AbstractClientDataTypeSpecControlAlgorithmClient(false), outgoingConnection = TestProbe().ref)))
      dataType ! ReceiveCallback((_) => {})
      val oldCallback = dataType.children.head
      val watcher = TestProbe()
      watcher.watch(oldCallback)

      dataType ! ReceiveCallback((_) => {})

      watcher.expectMsgPF(2.seconds) { case Terminated(ref) => ref should equal(oldCallback) }
    }

    "answer UpdateRequests with UpdateResponses" in {
      val dataTypeInstanceId = DataStructureInstanceId()
      val data = "{foo}"
      val operationId = OperationId()
      val dataType: TestActorRef[AbstractClientDataTypeTestClientDataStructure] = TestActorRef(
        Props(new AbstractClientDataTypeTestClientDataStructure(dataTypeInstanceId, new AbstractClientDataTypeSpecControlAlgorithmClient, outgoingConnection = TestProbe().ref)))
      dataType ! ReceiveCallback((_) => {})
      val operation = AbstractClientDataTypeSpecTestOperation(operationId, OperationContext(List.empty), ClientId(), data)
      val operationMessage = OperationMessage(ClientId(), dataTypeInstanceId, AbstractClientDataTypeSpec.dataTypeName, List(operation))

      dataType ! LocalOperationMessage(operationMessage)
      dataType ! UpdateRequest(ClientId(), dataTypeInstanceId)

      expectMsg(UpdateResponse(dataTypeInstanceId, AbstractClientDataTypeSpec.dataTypeName, data, Option(operationId)))
    }

    "answer UpdateRequests with the initial operation id when no operations were executed yet when being unacknowledged" in {
      val dataTypeInstanceId = DataStructureInstanceId()
      val operationId = OperationId()
      val dataType: TestActorRef[AbstractClientDataTypeTestClientDataStructure] = TestActorRef(
        Props(new AbstractClientDataTypeTestClientDataStructure(dataTypeInstanceId, new AbstractClientDataTypeSpecControlAlgorithmClient, Option(operationId), outgoingConnection = TestProbe().ref)))
      dataType ! ReceiveCallback((_) => {})

      dataType ! UpdateRequest(ClientId(), dataTypeInstanceId)

      expectMsg(UpdateResponse(dataTypeInstanceId, AbstractClientDataTypeSpec.dataTypeName, "{test}", Option(operationId)))
    }

    "answer UpdateRequests with the initial operation id when no operations were executed yet when being acknowledged" in {
      val dataTypeInstanceId = DataStructureInstanceId()
      val operationId = OperationId()
      val dataType: TestActorRef[AbstractClientDataTypeTestClientDataStructure] = TestActorRef(
        Props(new AbstractClientDataTypeTestClientDataStructure(dataTypeInstanceId, new AbstractClientDataTypeSpecControlAlgorithmClient, Option(operationId), outgoingConnection = TestProbe().ref)))
      dataType ! ReceiveCallback((_) => {})
      dataType ! CreateResponse(dataTypeInstanceId)

      dataType ! UpdateRequest(ClientId(), dataTypeInstanceId)

      expectMsg(UpdateResponse(dataTypeInstanceId, AbstractClientDataTypeSpec.dataTypeName, "{test}", Option(operationId)))
    }

    "send an HistoricOperationRequest after it receives a remote OperationMessage whose parent it does no know" in {
      val dataTypeInstanceId = DataStructureInstanceId()
      val data = "{foo}"
      val operationId = OperationId()
      val previousOperation = OperationId()
      val dataType: TestActorRef[AbstractClientDataTypeTestClientDataStructure] = TestActorRef(
        Props(new AbstractClientDataTypeTestClientDataStructure(dataTypeInstanceId, new AbstractClientDataTypeSpecControlAlgorithmClient, outgoingConnection = TestProbe().ref)))
      dataType ! ReceiveCallback((_) => {})
      dataType ! CreateResponse(dataTypeInstanceId)
      val operation = AbstractClientDataTypeSpecTestOperation(operationId, OperationContext(List(previousOperation)), ClientId(), data)
      val operationMessage = OperationMessage(ClientId(), dataTypeInstanceId, AbstractClientDataTypeSpec.dataTypeName, List(operation))

      dataType ! operationMessage

      val answer = expectMsgClass(classOf[HistoricOperationRequest])
      answer.sinceId should equal(null)
      answer.dataStructureInstanceId should equal(dataTypeInstanceId)
    }

    "must use the initial operation id for HistoricOperationsRequest if no other operation is present" in {
      val dataTypeInstanceId = DataStructureInstanceId()
      val data = "{foo}"
      val initialOperationId = OperationId()
      val operationId = OperationId()
      val previousOperation = OperationId()
      val dataType: TestActorRef[AbstractClientDataTypeTestClientDataStructure] = TestActorRef(
        Props(new AbstractClientDataTypeTestClientDataStructure(dataTypeInstanceId, new AbstractClientDataTypeSpecControlAlgorithmClient, Some(initialOperationId), outgoingConnection = TestProbe().ref)))
      dataType ! ReceiveCallback((_) => {})
      dataType ! CreateResponse(dataTypeInstanceId)
      val operation = AbstractClientDataTypeSpecTestOperation(operationId, OperationContext(List(previousOperation)), ClientId(), data)
      val operationMessage = OperationMessage(ClientId(), dataTypeInstanceId, AbstractClientDataTypeSpec.dataTypeName, List(operation))

      dataType ! operationMessage

      val answer = expectMsgClass(classOf[HistoricOperationRequest])
      answer.sinceId should equal(initialOperationId)
      answer.dataStructureInstanceId should equal(dataTypeInstanceId)
    }

    "must use the last operation id from a remote operation for a HistoricOperationsRequest if local operations happened in between" in {
      val dataTypeInstanceId = DataStructureInstanceId()
      val data = "{foo}"
      val lastArrivedRemoteOperationId = OperationId()
      val remoteOperationId = OperationId()
      val previousRemoteOperationId = OperationId()
      val dataType: TestActorRef[AbstractClientDataTypeTestClientDataStructure] = TestActorRef(
        Props(new AbstractClientDataTypeTestClientDataStructure(dataTypeInstanceId, new AbstractClientDataTypeSpecControlAlgorithmClient, outgoingConnection = TestProbe().ref)))
      dataType ! ReceiveCallback((_) => {})
      dataType ! CreateResponse(dataTypeInstanceId)
      val arrivedRemoteOperation = AbstractClientDataTypeSpecTestOperation(lastArrivedRemoteOperationId, OperationContext(), ClientId(), data)
      val arrivedRemoteOperationMessage = OperationMessage(ClientId(), dataTypeInstanceId, AbstractClientDataTypeSpec.dataTypeName, List(arrivedRemoteOperation))
      val localOperation = AbstractClientDataTypeSpecTestOperation(OperationId(), OperationContext(List(arrivedRemoteOperation.id)), ClientId(), data)
      val localOperationMessage = OperationMessage(ClientId(), dataTypeInstanceId, AbstractClientDataTypeSpec.dataTypeName, List(localOperation))
      val operationWithMissingPredecessor = AbstractClientDataTypeSpecTestOperation(remoteOperationId, OperationContext(List(previousRemoteOperationId)), ClientId(), data)
      val operationMessageWithMissingPredecessor = OperationMessage(ClientId(), dataTypeInstanceId, AbstractClientDataTypeSpec.dataTypeName, List(operationWithMissingPredecessor))

      dataType ! arrivedRemoteOperationMessage
      dataType ! LocalOperationMessage(localOperationMessage)
      dataType ! operationMessageWithMissingPredecessor

      val answer = expectMsgClass(classOf[HistoricOperationRequest])
      answer.sinceId should equal(lastArrivedRemoteOperationId)
      answer.dataStructureInstanceId should equal(dataTypeInstanceId)
    }

    "must use the operation id of an acknowledged local operation for a HistoricOperationsRequest if that was the last remote operation with WaveOT" in {
      val dataTypeInstanceId = DataStructureInstanceId()
      val data = "{foo}"
      val remoteOperationId = OperationId()
      val previousRemoteOperationId = OperationId()
      val dataType: TestActorRef[AbstractClientDataTypeTestClientDataStructure] = TestActorRef(
        Props(new AbstractClientDataTypeTestClientDataStructure(dataTypeInstanceId, new WaveOTClient((op) => {}), outgoingConnection = TestProbe().ref)))
      dataType ! ReceiveCallback((_) => {})
      dataType ! CreateResponse(dataTypeInstanceId)
      val localOperation = AbstractClientDataTypeSpecTestOperation(OperationId(), OperationContext(List()), ClientId(), data)
      val localOperationMessage = OperationMessage(ClientId(), dataTypeInstanceId, AbstractClientDataTypeSpec.dataTypeName, List(localOperation))
      val operationWithMissingPredecessor = AbstractClientDataTypeSpecTestOperation(remoteOperationId, OperationContext(List(previousRemoteOperationId)), ClientId(), data)
      val operationMessageWithMissingPredecessor = OperationMessage(ClientId(), dataTypeInstanceId, AbstractClientDataTypeSpec.dataTypeName, List(operationWithMissingPredecessor))

      dataType ! LocalOperationMessage(localOperationMessage)
      dataType ! localOperationMessage //the acknowledgement
      dataType ! operationMessageWithMissingPredecessor

      val answer = expectMsgClass(classOf[HistoricOperationRequest])
      answer.sinceId should equal(localOperation.id)
      answer.dataStructureInstanceId should equal(dataTypeInstanceId)
    }

    "apply the operations of an HistoricOperationRequest that have not been applied" in {
      val dataTypeInstanceId = DataStructureInstanceId()
      val data = "{foo}"
      val operationId = OperationId()
      val previousOperation = OperationId()
      val dataType: TestActorRef[AbstractClientDataTypeTestClientDataStructure] = TestActorRef(Props(new AbstractClientDataTypeTestClientDataStructure(dataTypeInstanceId, new AbstractClientDataTypeSpecControlAlgorithmClient, outgoingConnection = TestProbe().ref)))
      dataType ! ReceiveCallback((_) => {})
      dataType ! CreateResponse(dataTypeInstanceId)
      val normalOperation = AbstractClientDataTypeSpecTestOperation(operationId, OperationContext(List.empty), ClientId(), data)
      val missingParentOperation = AbstractClientDataTypeSpecTestOperation(OperationId(), OperationContext(List(previousOperation)), ClientId(), data)
      val missingOperation = AbstractClientDataTypeSpecTestOperation(previousOperation, OperationContext(List(operationId)), ClientId(), data)
      val operationMessage = OperationMessage(ClientId(), dataTypeInstanceId, AbstractClientDataTypeSpec.dataTypeName, List(normalOperation))
      val missingParentMessage = OperationMessage(ClientId(), dataTypeInstanceId, AbstractClientDataTypeSpec.dataTypeName, List(missingParentOperation))
      //let the data type first apply an operation
      dataType ! operationMessage

      dataType ! missingParentMessage

      val answer = expectMsgClass(classOf[HistoricOperationRequest])
      answer.sinceId should equal(operationId)

      dataType ! OperationMessage(ClientId(), dataTypeInstanceId, AbstractClientDataTypeSpec.dataTypeName, List(missingParentOperation, missingOperation))

      dataType.underlyingActor.historyBuffer.history should contain inOrder(missingParentOperation, missingOperation, normalOperation)
    }

    "must not apply duplicated received operations" in {
      val dataTypeInstanceId = DataStructureInstanceId()
      //because the control algorithm has to check for duplicates, we have to make sure the data type listens to it
      val controlAlgorithm = new ControlAlgorithmClient {

        override def canLocalOperationBeApplied(op: DataTypeOperation): Boolean = true

        override def transform(op: DataTypeOperation, history: HistoryBuffer, transformer: OperationTransformer): DataTypeOperation = op

        override def canBeApplied(op: DataTypeOperation, history: HistoryBuffer): Boolean = history.findOperation(op.id).isEmpty

        override def currentOperationContext: OperationContext = OperationContext(List.empty) //not important here
      }
      val dataType: TestActorRef[AbstractClientDataTypeTestClientDataStructure] = TestActorRef(
        Props(new AbstractClientDataTypeTestClientDataStructure(dataTypeInstanceId, controlAlgorithm, outgoingConnection = TestProbe().ref)))
      dataType ! ReceiveCallback((_) => {})
      dataType ! CreateResponse(dataTypeInstanceId)
      val data = "{foo}"
      val operation = AbstractClientDataTypeSpecTestOperation(OperationId(), OperationContext(List.empty), ClientId(), data)
      val operationMessage = OperationMessage(
        ClientId(),
        dataTypeInstanceId,
        AbstractClientDataTypeSpec.dataTypeName,
        List(operation)
      )

      dataType ! operationMessage
      dataType ! operationMessage

      dataType.underlyingActor.historyBuffer.history should equal(List(operation))
    }

    "must not apply duplicated received operations with WaveOT" in {
      val dataTypeInstanceId = DataStructureInstanceId()
      //because the control algorithm has to check for duplicates, we have to make sure the data type listens to it
      val dataType: TestActorRef[AbstractClientDataTypeTestClientDataStructure] = TestActorRef(
        Props(new AbstractClientDataTypeTestClientDataStructure(dataTypeInstanceId, new WaveOTClient((op) => {}), outgoingConnection = TestProbe().ref)))
      dataType ! ReceiveCallback((_) => {})
      dataType ! CreateResponse(dataTypeInstanceId)
      val data = "{foo}"
      val operation = AbstractClientDataTypeSpecTestOperation(OperationId(), OperationContext(List.empty), ClientId(), data)
      val operationMessage = OperationMessage(
        ClientId(),
        dataTypeInstanceId,
        AbstractClientDataTypeSpec.dataTypeName,
        List(operation)
      )

      dataType ! operationMessage
      dataType ! operationMessage

      dataType.underlyingActor.historyBuffer.history should equal(List(operation))
    }

    "buffer local operations until the CreateResponse comes" in {
      val dataTypeInstanceId = DataStructureInstanceId()
      val data = "{foo}"
      val outgoing = TestProbe()
      val dataType: TestActorRef[AbstractClientDataTypeTestClientDataStructure] = TestActorRef(Props(new AbstractClientDataTypeTestClientDataStructure(dataTypeInstanceId, new WaveOTClient(op => outgoing.ref ! op), outgoingConnection = outgoing.ref)))
      dataType ! ReceiveCallback((_) => {})
      val operation = AbstractClientDataTypeSpecTestOperation(OperationId(), OperationContext(List.empty), ClientId(), data)
      val operation2 = AbstractClientDataTypeSpecTestOperation(OperationId(), OperationContext(List.empty), ClientId(), data)
      val operationMessage = OperationMessage(ClientId(), dataTypeInstanceId, AbstractClientDataTypeSpec.dataTypeName, List(operation))
      val operationMessage2 = OperationMessage(ClientId(), dataTypeInstanceId, AbstractClientDataTypeSpec.dataTypeName, List(operation2))

      dataType ! LocalOperationMessage(operationMessage)
      dataType ! LocalOperationMessage(operationMessage2)
      dataType ! CreateResponse(dataTypeInstanceId)

      val toServer = outgoing.expectMsgClass(classOf[AbstractClientDataTypeSpecTestOperation])
    }


    "pass acknowledgements to the control algorithm" in {
      val outgoing = TestProbe()
      val dataTypeInstanceId = DataStructureInstanceId()
      val data = "{foo}"
      val operation = AbstractClientDataTypeSpecTestOperation(OperationId(), OperationContext(List.empty), ClientId(), data)
      val operation2 = AbstractClientDataTypeSpecTestOperation(OperationId(), OperationContext(List.empty), ClientId(), data)
      val operationMessage = OperationMessage(ClientId(), dataTypeInstanceId, AbstractClientDataTypeSpec.dataTypeName, List(operation))
      val operationMessage2 = OperationMessage(ClientId(), dataTypeInstanceId, AbstractClientDataTypeSpec.dataTypeName, List(operation2))
      val controlAlgorithm = new ControlAlgorithmClient {

        var correctOperationPassed = false

        override def canLocalOperationBeApplied(op: DataTypeOperation): Boolean = true

        override def transform(op: DataTypeOperation, history: HistoryBuffer, transformer: OperationTransformer): DataTypeOperation = op

        override def canBeApplied(op: DataTypeOperation, history: HistoryBuffer): Boolean = {
          if (op == operation) correctOperationPassed = true
          true
        }

        override def currentOperationContext: OperationContext = OperationContext(List.empty) // not important here
      }
      val dataType: TestActorRef[AbstractClientDataTypeTestClientDataStructure] = TestActorRef(
        Props(new AbstractClientDataTypeTestClientDataStructure(dataTypeInstanceId, controlAlgorithm, outgoingConnection = outgoing.ref)))
      dataType ! ReceiveCallback((_) => {})
      dataType ! CreateResponse(dataTypeInstanceId)

      dataType ! LocalOperationMessage(operationMessage)
      dataType ! LocalOperationMessage(operationMessage2)
      //acknowledgement
      dataType ! operationMessage

      controlAlgorithm.correctOperationPassed should be(true)
    }

    "pass local operations to the control algorithm when the CreateResponse comes" in {
      val dataTypeInstanceId = DataStructureInstanceId()
      val data = "{foo}"
      val outgoing = TestProbe()
      val operation = AbstractClientDataTypeSpecTestOperation(OperationId(), OperationContext(List.empty), ClientId(), data)
      val operation2 = AbstractClientDataTypeSpecTestOperation(OperationId(), OperationContext(List.empty), ClientId(), data)
      val operationMessage = OperationMessage(ClientId(), dataTypeInstanceId, AbstractClientDataTypeSpec.dataTypeName, List(operation))
      val operationMessage2 = OperationMessage(ClientId(), dataTypeInstanceId, AbstractClientDataTypeSpec.dataTypeName, List(operation2))

      val controlAlgorithm = new ControlAlgorithmClient {
        var first = true
        var correct = true

        override def canLocalOperationBeApplied(op: DataTypeOperation): Boolean = {
          if (first) {
            correct &= op == operation
            op should equal(operation)
            first = false
          } else {
            val comparisonOperation = AbstractClientDataTypeSpecTestOperation(operation2.id, OperationContext(List(operation.id)), operation2.clientId, operation2.data)
            correct &= op == comparisonOperation
            op should equal(comparisonOperation)
          }
          true
        }

        override def currentOperationContext: OperationContext = OperationContext(List.empty)

        override def transform(op: DataTypeOperation, history: HistoryBuffer, transformer: OperationTransformer): DataTypeOperation = op

        override def canBeApplied(op: DataTypeOperation, history: HistoryBuffer): Boolean = true
      }

      val dataType: TestActorRef[AbstractClientDataTypeTestClientDataStructure] = TestActorRef(Props(new AbstractClientDataTypeTestClientDataStructure(dataTypeInstanceId, controlAlgorithm, outgoingConnection = outgoing.ref)))
      dataType ! ReceiveCallback((_) => {})

      dataType ! LocalOperationMessage(operationMessage)
      dataType ! LocalOperationMessage(operationMessage2)
      dataType ! CreateResponse(dataTypeInstanceId)

      controlAlgorithm.correct should equal(true)
    }

    "add initialized lastOperationId to first local operation when being unacknowledged" in {
      val lastOperationId = OperationId()
      val dataType: TestActorRef[AbstractClientDataTypeTestClientDataStructure] = TestActorRef(
        Props(new AbstractClientDataTypeTestClientDataStructure(DataStructureInstanceId(), new AbstractClientDataTypeSpecControlAlgorithmClient, Option(lastOperationId), outgoingConnection = TestProbe().ref)))
      dataType ! ReceiveCallback((_) => {})
      val operation = AbstractClientDataTypeSpecTestOperation(OperationId(), OperationContext(List.empty), ClientId(), "")
      val operationMessage = OperationMessage(ClientId(), DataStructureInstanceId(), AbstractClientDataTypeSpec.dataTypeName, List(operation))

      dataType ! LocalOperationMessage(operationMessage)

      dataType.underlyingActor.historyBuffer.history.head should equal(AbstractClientDataTypeSpecTestOperation(operation.id, OperationContext(List(lastOperationId)), operation.clientId, operation.data))
    }

    "add initialized lastOperationId to first local operation when going from un- to acknowledged" in {
      val lastOperationId = OperationId()
      val dataType: TestActorRef[AbstractClientDataTypeTestClientDataStructure] = TestActorRef(
        Props(new AbstractClientDataTypeTestClientDataStructure(DataStructureInstanceId(), new AbstractClientDataTypeSpecControlAlgorithmClient, Option(lastOperationId), outgoingConnection = TestProbe().ref)))
      dataType ! ReceiveCallback((_) => {})
      dataType ! CreateResponse(DataStructureInstanceId())
      val operation = AbstractClientDataTypeSpecTestOperation(OperationId(), OperationContext(List.empty), ClientId(), "")
      val operationMessage = OperationMessage(ClientId(), DataStructureInstanceId(), AbstractClientDataTypeSpec.dataTypeName, List(operation))

      dataType ! LocalOperationMessage(operationMessage)

      dataType.underlyingActor.historyBuffer.history.head should equal(AbstractClientDataTypeSpecTestOperation(operation.id, OperationContext(List(lastOperationId)), operation.clientId, operation.data))
    }

    "add initialized lastOperationId to first local operation when being a remote instantiation" in {
      val lastOperationId = OperationId()
      val dataType: TestActorRef[AbstractClientDataTypeTestClientDataStructure] = TestActorRef(
        Props(new AbstractClientDataTypeTestClientDataStructure(DataStructureInstanceId(), new AbstractClientDataTypeSpecControlAlgorithmClient, Option(lastOperationId), outgoingConnection = TestProbe().ref)))
      dataType ! RemoteInstantiation
      dataType ! ReceiveCallback((_) => {})
      val operation = AbstractClientDataTypeSpecTestOperation(OperationId(), OperationContext(List.empty), ClientId(), "")
      val operationMessage = OperationMessage(ClientId(), DataStructureInstanceId(), AbstractClientDataTypeSpec.dataTypeName, List(operation))

      dataType ! LocalOperationMessage(operationMessage)

      dataType.underlyingActor.historyBuffer.history.head should equal(AbstractClientDataTypeSpecTestOperation(operation.id, OperationContext(List(lastOperationId)), operation.clientId, operation.data))
    }

    "ignore messages until callback is set when being a remote instantiation" in {
      val dataType: TestActorRef[AbstractClientDataTypeTestClientDataStructure] = TestActorRef(
        Props(new AbstractClientDataTypeTestClientDataStructure(DataStructureInstanceId(), new AbstractClientDataTypeSpecControlAlgorithmClient, outgoingConnection = TestProbe().ref)))
      val operationMessage = OperationMessage(
        ClientId(),
        DataStructureInstanceId(),
        AbstractClientDataTypeSpec.dataTypeName,
        List(
          AbstractClientDataTypeSpecTestOperation(OperationId(), OperationContext(List.empty), ClientId(), "")
        )
      )
      dataType ! RemoteInstantiation

      dataType ! LocalOperationMessage(operationMessage)
      expectNoMsg()
      dataType ! operationMessage
      expectNoMsg()
      dataType ! UpdateRequest(ClientId(), DataStructureInstanceId())
      expectNoMsg()

      dataType.underlyingActor.historyBuffer.history shouldBe empty
    }

    "consider the initial operation id when searching for context dependency" in {
      val lastOperationId = OperationId()
      val dataType: TestActorRef[AbstractClientDataTypeTestClientDataStructure] = TestActorRef(
        Props(new AbstractClientDataTypeTestClientDataStructure(DataStructureInstanceId(), new AbstractClientDataTypeSpecControlAlgorithmClient, Option(lastOperationId), outgoingConnection = TestProbe().ref)))
      dataType ! ReceiveCallback((_) => {})
      dataType ! CreateResponse(DataStructureInstanceId())
      val operation = AbstractClientDataTypeSpecTestOperation(OperationId(), OperationContext(List(lastOperationId)), ClientId(), "")
      val operationMessage = OperationMessage(ClientId(), DataStructureInstanceId(), AbstractClientDataTypeSpec.dataTypeName, List(operation))

      dataType ! operationMessage

      //if the data type ignores the lastOperationId as context of the OperationMessage it'll send a HistoricOperationRequest
      expectNoMsg()
    }

    "not remove the inFlightOperation if it is the second operation within the message before transforming the first one against it with WaveOT" in {
      val dataTypeInstanceId = DataStructureInstanceId()
      val data = "{foo}"
      //create own transformer so the context update happens
      val testTransformer = new OperationTransformer {
        override def transform(pair: (DataTypeOperation, DataTypeOperation)): DataTypeOperation = {
          val operation = pair._1.asInstanceOf[AbstractClientDataTypeSpecTestOperation]
          AbstractClientDataTypeSpecTestOperation(operation.id, OperationContext(List(pair._2.id)), operation.clientId, operation.data)
        }

        override protected def transformInternal(pair: (DataTypeOperation, DataTypeOperation), withNewContext: Boolean): DataTypeOperation = {
          pair._1
        }
      }
      val dataType: TestActorRef[AbstractClientDataTypeTestClientDataStructure] = TestActorRef(Props(new AbstractClientDataTypeTestClientDataStructure(dataTypeInstanceId, new WaveOTClient((op) => {}), outgoingConnection = TestProbe().ref) {
        override val transformer = testTransformer
      }))
      dataType ! ReceiveCallback((_) => {})
      dataType ! CreateResponse(dataTypeInstanceId)
      val localOperation = AbstractClientDataTypeSpecTestOperation(OperationId(), OperationContext(List.empty), ClientId(), data)
      val localOperationMsg = OperationMessage(ClientId(), dataTypeInstanceId, AbstractClientDataTypeSpec.dataTypeName, List(localOperation))
      val additionalOperation = AbstractClientDataTypeSpecTestOperation(OperationId(), OperationContext(List.empty), ClientId(), data)
      val localOperationWithChangedContext = AbstractClientDataTypeSpecTestOperation(localOperation.id, OperationContext(List(additionalOperation.id)), localOperation.clientId, localOperation.data)

      dataType ! LocalOperationMessage(localOperationMsg)

      dataType ! OperationMessage(ClientId(), dataTypeInstanceId, AbstractClientDataTypeSpec.dataTypeName, List(localOperationWithChangedContext, additionalOperation))

      dataType.underlyingActor.historyBuffer.history.find(op => op.id == additionalOperation.id).get.operationContext should equal(OperationContext(List(localOperation.id)))
    }


    "generate a LocalOperationEvent after applying a local operation when unacknowledged" in {
      val dataTypeInstanceId = DataStructureInstanceId()
      val data = "{foo}"
      val latch = new CountDownLatch(1)
      val operation = AbstractClientDataTypeSpecTestOperation(OperationId(), OperationContext(List.empty), ClientId(), data)
      val dataType: TestActorRef[AbstractClientDataTypeTestClientDataStructure] = TestActorRef(
        Props(new AbstractClientDataTypeTestClientDataStructure(dataTypeInstanceId, new AbstractClientDataTypeSpecControlAlgorithmClient, outgoingConnection = TestProbe().ref)))
      dataType ! ReceiveCallback((event: ClientDataTypeEvent) => {
        event.asInstanceOf[LocalOperationEvent].operation should equal(operation)
        latch.countDown()
      })
      val operationMessage = OperationMessage(
        ClientId(),
        dataTypeInstanceId,
        AbstractClientDataTypeSpec.dataTypeName,
        List(operation)
      )

      dataType ! LocalOperationMessage(operationMessage)

      val awaiting = latch.await(5, TimeUnit.SECONDS)
      awaiting should equal(true)
    }

    "generate a LocalOperationEvent after applying a local operation when acknowledged" in {
      val dataTypeInstanceId = DataStructureInstanceId()
      val data = "{foo}"
      val latch = new CountDownLatch(1)
      val operation = AbstractClientDataTypeSpecTestOperation(OperationId(), OperationContext(List.empty), ClientId(), data)
      val dataType: TestActorRef[AbstractClientDataTypeTestClientDataStructure] = TestActorRef(
        Props(new AbstractClientDataTypeTestClientDataStructure(dataTypeInstanceId, new AbstractClientDataTypeSpecControlAlgorithmClient, outgoingConnection = TestProbe().ref)))
      dataType ! RemoteInstantiation
      dataType ! ReceiveCallback((event: ClientDataTypeEvent) => {
        event.asInstanceOf[LocalOperationEvent].operation should equal(operation)
        latch.countDown()
      })
      val operationMessage = OperationMessage(
        ClientId(),
        dataTypeInstanceId,
        AbstractClientDataTypeSpec.dataTypeName,
        List(operation)
      )

      dataType ! LocalOperationMessage(operationMessage)

      val awaiting = latch.await(5, TimeUnit.SECONDS)
      awaiting should equal(true)
    }

    "generate a RemoteOperationEvent after applying a remote operation when acknowledged" in {
      val dataTypeInstanceId = DataStructureInstanceId()
      val data = "{foo}"
      val latch = new CountDownLatch(1)
      val operation = AbstractClientDataTypeSpecTestOperation(OperationId(), OperationContext(List.empty), ClientId(), data)
      val dataType: TestActorRef[AbstractClientDataTypeTestClientDataStructure] = TestActorRef(
        Props(new AbstractClientDataTypeTestClientDataStructure(dataTypeInstanceId, new AbstractClientDataTypeSpecControlAlgorithmClient, outgoingConnection = TestProbe().ref)))
      dataType ! RemoteInstantiation
      dataType ! ReceiveCallback((event: ClientDataTypeEvent) => {
        event.asInstanceOf[RemoteOperationEvent].operation should equal(operation)
        latch.countDown()
      })
      val operationMessage = OperationMessage(
        ClientId(),
        dataTypeInstanceId,
        AbstractClientDataTypeSpec.dataTypeName,
        List(operation)
      )

      dataType ! operationMessage

      val awaiting = latch.await(5, TimeUnit.SECONDS)
      awaiting should equal(true)
    }

    "generate a CreateResponseEvent after receiving a CreateResponse" in {
      val dataTypeInstanceId = DataStructureInstanceId()
      val data = "{foo}"
      val latch = new CountDownLatch(1)
      val dataType: TestActorRef[AbstractClientDataTypeTestClientDataStructure] = TestActorRef(
        Props(new AbstractClientDataTypeTestClientDataStructure(dataTypeInstanceId, new AbstractClientDataTypeSpecControlAlgorithmClient, outgoingConnection = TestProbe().ref)))
      dataType ! ReceiveCallback((event: ClientDataTypeEvent) => {
        event should equal(CreateResponseEvent(dataTypeInstanceId))
        latch.countDown()
      })

      dataType ! CreateResponse(dataTypeInstanceId)

      val awaiting = latch.await(5, TimeUnit.SECONDS)
      awaiting should equal(true)
    }

    "generate an AcknowledgementEvent when receiving an remote operation and the control algorithm states it cannot be applied" in {
      val dataTypeInstanceId = DataStructureInstanceId()
      val data = "{foo}"
      val latch = new CountDownLatch(1)
      val operation = AbstractClientDataTypeSpecTestOperation(OperationId(), OperationContext(List.empty), ClientId(), data)
      val dataType: TestActorRef[AbstractClientDataTypeTestClientDataStructure] = TestActorRef(
        Props(new AbstractClientDataTypeTestClientDataStructure(dataTypeInstanceId, new AbstractClientDataTypeSpecControlAlgorithmClient(canRemoteBeApplied = false), outgoingConnection = TestProbe().ref)))
      dataType ! RemoteInstantiation
      dataType ! ReceiveCallback((event: ClientDataTypeEvent) => {
        event.asInstanceOf[AcknowledgementEvent].operation should equal(operation)
        latch.countDown()
      })
      val operationMessage = OperationMessage(
        ClientId(),
        dataTypeInstanceId,
        AbstractClientDataTypeSpec.dataTypeName,
        List(operation)
      )

      dataType ! operationMessage

      val awaiting = latch.await(5, TimeUnit.SECONDS)
      awaiting should equal(true)
    }
  }
}

class AbstractClientDataTypeTestClientDataStructure(
                                                dataStructureInstanceId: DataStructureInstanceId,
                                                clientControlAlgorithm: ControlAlgorithmClient,
                                                lastOperationId: Option[OperationId] = Option.empty,
                                                outgoingConnection: ActorRef)
  extends AbstractClientDataStructure(dataStructureInstanceId, clientControlAlgorithm, lastOperationId, outgoingConnection) {

  var data = "{test}"

  override val dataTypeName: DataStructureName = AbstractClientDataTypeSpec.dataTypeName

  override val transformer: OperationTransformer = new OperationTransformer {
    override def transform(pair: (DataTypeOperation, DataTypeOperation)): DataTypeOperation = pair._1

    override def bulkTransform(operation: DataTypeOperation, bridge: List[DataTypeOperation]): List[DataTypeOperation] = bridge

    override protected def transformInternal(pair: (DataTypeOperation, DataTypeOperation), withNewContext: Boolean): DataTypeOperation = pair._1
  }

  override def apply(op: DataTypeOperation): Unit = {
    op match {
      case test: AbstractClientDataTypeSpecTestOperation => data = test.data
      case _ => fail
    }
  }

  override def getDataAsJson: String = data

  override def cloneOperationWithNewContext(op: DataTypeOperation, context: OperationContext): DataTypeOperation = {
    op match {
      case abstr: AbstractClientDataTypeSpecTestOperation => AbstractClientDataTypeSpecTestOperation(abstr.id, context, abstr.clientId, abstr.data)
    }
  }
}

case class AbstractClientDataTypeSpecTestOperation(id: OperationId, operationContext: OperationContext, var clientId: ClientId, data: String) extends DataTypeOperation

object AbstractClientDataTypeSpec {
  val dataTypeName = DataStructureName("AbstractClientDataType")
}

class AbstractClientDataTypeSpecControlAlgorithmClient(canRemoteBeApplied: Boolean = true) extends ControlAlgorithmClient {

  var didTransform = false

  var context: List[OperationId] = List.empty

  override def canLocalOperationBeApplied(op: DataTypeOperation): Boolean = {
    context = List(op.id)
    true
  }

  override def canBeApplied(op: DataTypeOperation, history: HistoryBuffer): Boolean = canRemoteBeApplied

  override def transform(op: DataTypeOperation, history: HistoryBuffer, transformer: OperationTransformer): DataTypeOperation = {
    context = List(op.id)
    didTransform = true
    op
  }

  override def currentOperationContext: OperationContext = {
    OperationContext(context)
  }
}