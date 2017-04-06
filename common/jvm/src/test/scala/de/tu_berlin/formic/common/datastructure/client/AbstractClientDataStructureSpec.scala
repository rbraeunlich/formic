package de.tu_berlin.formic.common.datastructure.client

import java.util.concurrent.{CountDownLatch, TimeUnit}

import akka.actor.{ActorRef, ActorSystem, Props, Terminated}
import akka.testkit.{ImplicitSender, TestActorRef, TestKit, TestProbe}
import de.tu_berlin.formic.common.controlalgo.{ControlAlgorithmClient, WaveOTClient}
import de.tu_berlin.formic.common.datastructure.FormicDataStructure.LocalOperationMessage
import de.tu_berlin.formic.common.datastructure._
import de.tu_berlin.formic.common.datastructure.client.AbstractClientDataStructure.{ReceiveCallback, RemoteInstantiation}
import de.tu_berlin.formic.common.message._
import de.tu_berlin.formic.common.{ClientId, DataStructureInstanceId, OperationId}
import org.scalatest.Assertions._
import org.scalatest.{Matchers, WordSpecLike}

import scala.concurrent.duration._

/**
  * @author Ronny Bräunlich
  */
class AbstractClientDataStructureSpec extends TestKit(ActorSystem("AbstractClientDataStructureSpec"))
  with WordSpecLike
  with ImplicitSender
  with StopSystemAfterAll
  with Matchers {

  "AbstractClientDataStructureSpec" must {

    "ignore messages until callback is set" in {
      val dataType: TestActorRef[AbstractClientDataStructureTestClientDataStructure] = TestActorRef(
        Props(new AbstractClientDataStructureTestClientDataStructure(DataStructureInstanceId(), new AbstractClientDataStructureSpecControlAlgorithmClient, outgoingConnection = TestProbe().ref)))
      val operationMessage = OperationMessage(
        ClientId(),
        DataStructureInstanceId(),
        AbstractClientDataStructureSpec.dataTypeName,
        List(
          AbstractClientDataStructureSpecTestOperation(OperationId(), OperationContext(List.empty), ClientId(), "")
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
      val dataType: TestActorRef[AbstractClientDataStructureTestClientDataStructure] = TestActorRef(
        Props(new AbstractClientDataStructureTestClientDataStructure(dataTypeInstanceId, new AbstractClientDataStructureSpecControlAlgorithmClient, outgoingConnection = TestProbe().ref)))
      dataType ! ReceiveCallback((_) => {})
      val operationMessage = OperationMessage(
        ClientId(),
        dataTypeInstanceId,
        AbstractClientDataStructureSpec.dataTypeName,
        List(
          AbstractClientDataStructureSpecTestOperation(OperationId(), OperationContext(List.empty), ClientId(), data)
        )
      )

      dataType ! LocalOperationMessage(operationMessage)

      dataType.underlyingActor.data should equal(data)
    }

    "not transform local operations" in {
      val dataTypeInstanceId = DataStructureInstanceId()
      val data = "{foo}"
      val controlAlgo = new AbstractClientDataStructureSpecControlAlgorithmClient
      val dataType: TestActorRef[AbstractClientDataStructureTestClientDataStructure] = TestActorRef(
        Props(new AbstractClientDataStructureTestClientDataStructure(dataTypeInstanceId, controlAlgo, outgoingConnection = TestProbe().ref)))
      dataType ! ReceiveCallback((_) => {})
      val operationMessage = OperationMessage(
        ClientId(),
        dataTypeInstanceId,
        AbstractClientDataStructureSpec.dataTypeName,
        List(
          AbstractClientDataStructureSpecTestOperation(OperationId(), OperationContext(List.empty), ClientId(), data)
        )
      )

      dataType ! LocalOperationMessage(operationMessage)

      controlAlgo.didTransform should equal(false)
    }

    "add local operations to the history buffer" in {
      val dataTypeInstanceId = DataStructureInstanceId()
      val data = "{foo}"
      val dataType: TestActorRef[AbstractClientDataStructureTestClientDataStructure] = TestActorRef(
        Props(new AbstractClientDataStructureTestClientDataStructure(dataTypeInstanceId, new AbstractClientDataStructureSpecControlAlgorithmClient, outgoingConnection = TestProbe().ref)))
      dataType ! ReceiveCallback((_) => {})
      val operation = AbstractClientDataStructureSpecTestOperation(OperationId(), OperationContext(List.empty), ClientId(), data)
      val operationMessage = OperationMessage(
        ClientId(),
        dataTypeInstanceId,
        AbstractClientDataStructureSpec.dataTypeName,
        List(operation)
      )

      dataType ! LocalOperationMessage(operationMessage)

      dataType.underlyingActor.historyBuffer.history should contain(operation)
    }

    "update the operation context of local operations when unacknowledged" in {
      val dataTypeInstanceId = DataStructureInstanceId()
      val data = "{foo}"
      val dataType: TestActorRef[AbstractClientDataStructureTestClientDataStructure] = TestActorRef(
        Props(new AbstractClientDataStructureTestClientDataStructure(dataTypeInstanceId, new AbstractClientDataStructureSpecControlAlgorithmClient, outgoingConnection = TestProbe().ref)))
      dataType ! ReceiveCallback((_) => {})
      val operation = AbstractClientDataStructureSpecTestOperation(OperationId(), OperationContext(List.empty), ClientId(), data)
      val operation2 = AbstractClientDataStructureSpecTestOperation(OperationId(), OperationContext(List.empty), ClientId(), data)
      val operationMessage = OperationMessage(
        ClientId(),
        dataTypeInstanceId,
        AbstractClientDataStructureSpec.dataTypeName,
        List(operation)
      )
      val operationMessage2 = OperationMessage(
        ClientId(),
        dataTypeInstanceId,
        AbstractClientDataStructureSpec.dataTypeName,
        List(operation2)
      )

      dataType ! LocalOperationMessage(operationMessage)
      dataType ! LocalOperationMessage(operationMessage2)

      dataType.underlyingActor.historyBuffer.history.head should equal(AbstractClientDataStructureSpecTestOperation(operation2.id, OperationContext(List(operation.id)), operation2.clientId, operation2.data))
    }

    "update the operation context of local operations when acknowledged using the control algorithm" in {
      val dataTypeInstanceId = DataStructureInstanceId()
      val data = "{foo}"
      val dataType: TestActorRef[AbstractClientDataStructureTestClientDataStructure] = TestActorRef(
        Props(new AbstractClientDataStructureTestClientDataStructure(dataTypeInstanceId, new AbstractClientDataStructureSpecControlAlgorithmClient, outgoingConnection = TestProbe().ref)))
      dataType ! ReceiveCallback((_) => {})
      dataType ! CreateResponse(dataTypeInstanceId)
      val operation = AbstractClientDataStructureSpecTestOperation(OperationId(), OperationContext(List.empty), ClientId(), data)
      val operation2 = AbstractClientDataStructureSpecTestOperation(OperationId(), OperationContext(List.empty), ClientId(), data)
      val operationMessage = OperationMessage(
        ClientId(),
        dataTypeInstanceId,
        AbstractClientDataStructureSpec.dataTypeName,
        List(operation)
      )
      val operationMessage2 = OperationMessage(
        ClientId(),
        dataTypeInstanceId,
        AbstractClientDataStructureSpec.dataTypeName,
        List(operation2)
      )

      dataType ! LocalOperationMessage(operationMessage)
      dataType ! LocalOperationMessage(operationMessage2)

      dataType.underlyingActor.historyBuffer.history.head should equal(AbstractClientDataStructureSpecTestOperation(operation2.id, OperationContext(List(operation.id)), operation2.clientId, operation2.data))
    }

    "add remote operations to the history buffer" in {
      val dataTypeInstanceId = DataStructureInstanceId()
      val data = "{foo}"
      val dataType: TestActorRef[AbstractClientDataStructureTestClientDataStructure] = TestActorRef(
        Props(new AbstractClientDataStructureTestClientDataStructure(dataTypeInstanceId, new AbstractClientDataStructureSpecControlAlgorithmClient, outgoingConnection = TestProbe().ref)))
      dataType ! ReceiveCallback((_) => {})
      dataType ! CreateResponse(dataTypeInstanceId)
      val operation = AbstractClientDataStructureSpecTestOperation(OperationId(), OperationContext(List.empty), ClientId(), data)
      val operationMessage = OperationMessage(
        ClientId(),
        dataTypeInstanceId,
        AbstractClientDataStructureSpec.dataTypeName,
        List(operation)
      )

      dataType ! operationMessage

      dataType.underlyingActor.historyBuffer.history should contain(operation)
    }

    "apply received operations from an operation message" in {
      val dataTypeInstanceId = DataStructureInstanceId()
      val dataType: TestActorRef[AbstractClientDataStructureTestClientDataStructure] = TestActorRef(
        Props(new AbstractClientDataStructureTestClientDataStructure(dataTypeInstanceId, new AbstractClientDataStructureSpecControlAlgorithmClient, outgoingConnection = TestProbe().ref)))
      dataType ! ReceiveCallback((_) => {})
      val data = "{foo}"
      val operation = AbstractClientDataStructureSpecTestOperation(OperationId(), OperationContext(List.empty), ClientId(), data)
      val operationMessage = OperationMessage(
        ClientId(),
        dataTypeInstanceId,
        AbstractClientDataStructureSpec.dataTypeName,
        List(operation)
      )

      dataType ! LocalOperationMessage(operationMessage)

      dataType.underlyingActor.data should equal(data)
    }

    "not apply operations when the ControlAlgorithm states they are not ready and store them" in {
      val dataTypeInstanceId = DataStructureInstanceId()
      val dataType: TestActorRef[AbstractClientDataStructureTestClientDataStructure] = TestActorRef(
        Props(new AbstractClientDataStructureTestClientDataStructure(dataTypeInstanceId, new AbstractClientDataStructureSpecControlAlgorithmClient(false), outgoingConnection = TestProbe().ref)))
      dataType ! ReceiveCallback((_) => {})
      dataType ! CreateResponse(dataTypeInstanceId)
      val operation = AbstractClientDataStructureSpecTestOperation(OperationId(), OperationContext(List.empty), ClientId(), "{}")
      val message = OperationMessage(
        ClientId(),
        DataStructureInstanceId(),
        AbstractClientDataStructureSpec.dataTypeName,
        List(operation)
      )
      dataType ! message

      dataType.underlyingActor.historyBuffer.history should not contain operation
    }

    "pass operations to the control algorithm for transformation" in {
      var hasBeenTransformed = false
      val controlAlgo: ControlAlgorithmClient = new ControlAlgorithmClient {
        override def transform(op: DataStructureOperation, history: HistoryBuffer, transformer: OperationTransformer): DataStructureOperation = {
          hasBeenTransformed = true
          op
        }

        override def canBeApplied(op: DataStructureOperation, history: HistoryBuffer): Boolean = {
          true
        }

        override def canLocalOperationBeApplied(op: DataStructureOperation): Boolean = true

        override def currentOperationContext: OperationContext = OperationContext(List.empty) //not important here
      }

      val dataType: TestActorRef[AbstractClientDataStructureTestClientDataStructure] = TestActorRef(
        Props(new AbstractClientDataStructureTestClientDataStructure(DataStructureInstanceId(), controlAlgo, outgoingConnection = TestProbe().ref)))
      dataType ! ReceiveCallback((_) => {})
      dataType ! CreateResponse(DataStructureInstanceId())
      val operation = AbstractClientDataStructureSpecTestOperation(OperationId(), OperationContext(List.empty), ClientId(), "{1}")
      val message = OperationMessage(
        ClientId(),
        DataStructureInstanceId(),
        AbstractClientDataStructureSpec.dataTypeName,
        List(operation)
      )

      dataType ! message

      hasBeenTransformed should be(true)
    }

    "replace a callback when receiving a new one and kill the old one" in {
      val dataType: TestActorRef[AbstractClientDataStructureTestClientDataStructure] = TestActorRef(
        Props(new AbstractClientDataStructureTestClientDataStructure(DataStructureInstanceId(), new AbstractClientDataStructureSpecControlAlgorithmClient(false), outgoingConnection = TestProbe().ref)))
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
      val dataType: TestActorRef[AbstractClientDataStructureTestClientDataStructure] = TestActorRef(
        Props(new AbstractClientDataStructureTestClientDataStructure(dataTypeInstanceId, new AbstractClientDataStructureSpecControlAlgorithmClient, outgoingConnection = TestProbe().ref)))
      dataType ! ReceiveCallback((_) => {})
      val operation = AbstractClientDataStructureSpecTestOperation(operationId, OperationContext(List.empty), ClientId(), data)
      val operationMessage = OperationMessage(ClientId(), dataTypeInstanceId, AbstractClientDataStructureSpec.dataTypeName, List(operation))

      dataType ! LocalOperationMessage(operationMessage)
      dataType ! UpdateRequest(ClientId(), dataTypeInstanceId)

      expectMsg(UpdateResponse(dataTypeInstanceId, AbstractClientDataStructureSpec.dataTypeName, data, Option(operationId)))
    }

    "answer UpdateRequests with the initial operation id when no operations were executed yet when being unacknowledged" in {
      val dataTypeInstanceId = DataStructureInstanceId()
      val operationId = OperationId()
      val dataType: TestActorRef[AbstractClientDataStructureTestClientDataStructure] = TestActorRef(
        Props(new AbstractClientDataStructureTestClientDataStructure(dataTypeInstanceId, new AbstractClientDataStructureSpecControlAlgorithmClient, Option(operationId), outgoingConnection = TestProbe().ref)))
      dataType ! ReceiveCallback((_) => {})

      dataType ! UpdateRequest(ClientId(), dataTypeInstanceId)

      expectMsg(UpdateResponse(dataTypeInstanceId, AbstractClientDataStructureSpec.dataTypeName, "{test}", Option(operationId)))
    }

    "answer UpdateRequests with the initial operation id when no operations were executed yet when being acknowledged" in {
      val dataTypeInstanceId = DataStructureInstanceId()
      val operationId = OperationId()
      val dataType: TestActorRef[AbstractClientDataStructureTestClientDataStructure] = TestActorRef(
        Props(new AbstractClientDataStructureTestClientDataStructure(dataTypeInstanceId, new AbstractClientDataStructureSpecControlAlgorithmClient, Option(operationId), outgoingConnection = TestProbe().ref)))
      dataType ! ReceiveCallback((_) => {})
      dataType ! CreateResponse(dataTypeInstanceId)

      dataType ! UpdateRequest(ClientId(), dataTypeInstanceId)

      expectMsg(UpdateResponse(dataTypeInstanceId, AbstractClientDataStructureSpec.dataTypeName, "{test}", Option(operationId)))
    }

    "send an HistoricOperationRequest after it receives a remote OperationMessage whose parent it does no know" in {
      val dataTypeInstanceId = DataStructureInstanceId()
      val data = "{foo}"
      val operationId = OperationId()
      val previousOperation = OperationId()
      val dataType: TestActorRef[AbstractClientDataStructureTestClientDataStructure] = TestActorRef(
        Props(new AbstractClientDataStructureTestClientDataStructure(dataTypeInstanceId, new AbstractClientDataStructureSpecControlAlgorithmClient, outgoingConnection = TestProbe().ref)))
      dataType ! ReceiveCallback((_) => {})
      dataType ! CreateResponse(dataTypeInstanceId)
      val operation = AbstractClientDataStructureSpecTestOperation(operationId, OperationContext(List(previousOperation)), ClientId(), data)
      val operationMessage = OperationMessage(ClientId(), dataTypeInstanceId, AbstractClientDataStructureSpec.dataTypeName, List(operation))

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
      val dataType: TestActorRef[AbstractClientDataStructureTestClientDataStructure] = TestActorRef(
        Props(new AbstractClientDataStructureTestClientDataStructure(dataTypeInstanceId, new AbstractClientDataStructureSpecControlAlgorithmClient, Some(initialOperationId), outgoingConnection = TestProbe().ref)))
      dataType ! ReceiveCallback((_) => {})
      dataType ! CreateResponse(dataTypeInstanceId)
      val operation = AbstractClientDataStructureSpecTestOperation(operationId, OperationContext(List(previousOperation)), ClientId(), data)
      val operationMessage = OperationMessage(ClientId(), dataTypeInstanceId, AbstractClientDataStructureSpec.dataTypeName, List(operation))

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
      val dataType: TestActorRef[AbstractClientDataStructureTestClientDataStructure] = TestActorRef(
        Props(new AbstractClientDataStructureTestClientDataStructure(dataTypeInstanceId, new AbstractClientDataStructureSpecControlAlgorithmClient, outgoingConnection = TestProbe().ref)))
      dataType ! ReceiveCallback((_) => {})
      dataType ! CreateResponse(dataTypeInstanceId)
      val arrivedRemoteOperation = AbstractClientDataStructureSpecTestOperation(lastArrivedRemoteOperationId, OperationContext(), ClientId(), data)
      val arrivedRemoteOperationMessage = OperationMessage(ClientId(), dataTypeInstanceId, AbstractClientDataStructureSpec.dataTypeName, List(arrivedRemoteOperation))
      val localOperation = AbstractClientDataStructureSpecTestOperation(OperationId(), OperationContext(List(arrivedRemoteOperation.id)), ClientId(), data)
      val localOperationMessage = OperationMessage(ClientId(), dataTypeInstanceId, AbstractClientDataStructureSpec.dataTypeName, List(localOperation))
      val operationWithMissingPredecessor = AbstractClientDataStructureSpecTestOperation(remoteOperationId, OperationContext(List(previousRemoteOperationId)), ClientId(), data)
      val operationMessageWithMissingPredecessor = OperationMessage(ClientId(), dataTypeInstanceId, AbstractClientDataStructureSpec.dataTypeName, List(operationWithMissingPredecessor))

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
      val dataType: TestActorRef[AbstractClientDataStructureTestClientDataStructure] = TestActorRef(
        Props(new AbstractClientDataStructureTestClientDataStructure(dataTypeInstanceId, new WaveOTClient((op) => {}), outgoingConnection = TestProbe().ref)))
      dataType ! ReceiveCallback((_) => {})
      dataType ! CreateResponse(dataTypeInstanceId)
      val localOperation = AbstractClientDataStructureSpecTestOperation(OperationId(), OperationContext(List()), ClientId(), data)
      val localOperationMessage = OperationMessage(ClientId(), dataTypeInstanceId, AbstractClientDataStructureSpec.dataTypeName, List(localOperation))
      val operationWithMissingPredecessor = AbstractClientDataStructureSpecTestOperation(remoteOperationId, OperationContext(List(previousRemoteOperationId)), ClientId(), data)
      val operationMessageWithMissingPredecessor = OperationMessage(ClientId(), dataTypeInstanceId, AbstractClientDataStructureSpec.dataTypeName, List(operationWithMissingPredecessor))

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
      val dataType: TestActorRef[AbstractClientDataStructureTestClientDataStructure] = TestActorRef(Props(new AbstractClientDataStructureTestClientDataStructure(dataTypeInstanceId, new AbstractClientDataStructureSpecControlAlgorithmClient, outgoingConnection = TestProbe().ref)))
      dataType ! ReceiveCallback((_) => {})
      dataType ! CreateResponse(dataTypeInstanceId)
      val normalOperation = AbstractClientDataStructureSpecTestOperation(operationId, OperationContext(List.empty), ClientId(), data)
      val missingParentOperation = AbstractClientDataStructureSpecTestOperation(OperationId(), OperationContext(List(previousOperation)), ClientId(), data)
      val missingOperation = AbstractClientDataStructureSpecTestOperation(previousOperation, OperationContext(List(operationId)), ClientId(), data)
      val operationMessage = OperationMessage(ClientId(), dataTypeInstanceId, AbstractClientDataStructureSpec.dataTypeName, List(normalOperation))
      val missingParentMessage = OperationMessage(ClientId(), dataTypeInstanceId, AbstractClientDataStructureSpec.dataTypeName, List(missingParentOperation))
      //let the data type first apply an operation
      dataType ! operationMessage

      dataType ! missingParentMessage

      val answer = expectMsgClass(classOf[HistoricOperationRequest])
      answer.sinceId should equal(operationId)

      dataType ! OperationMessage(ClientId(), dataTypeInstanceId, AbstractClientDataStructureSpec.dataTypeName, List(missingParentOperation, missingOperation))

      dataType.underlyingActor.historyBuffer.history should contain inOrder(missingParentOperation, missingOperation, normalOperation)
    }

    "must not apply duplicated received operations" in {
      val dataTypeInstanceId = DataStructureInstanceId()
      //because the control algorithm has to check for duplicates, we have to make sure the data type listens to it
      val controlAlgorithm = new ControlAlgorithmClient {

        override def canLocalOperationBeApplied(op: DataStructureOperation): Boolean = true

        override def transform(op: DataStructureOperation, history: HistoryBuffer, transformer: OperationTransformer): DataStructureOperation = op

        override def canBeApplied(op: DataStructureOperation, history: HistoryBuffer): Boolean = history.findOperation(op.id).isEmpty

        override def currentOperationContext: OperationContext = OperationContext(List.empty) //not important here
      }
      val dataType: TestActorRef[AbstractClientDataStructureTestClientDataStructure] = TestActorRef(
        Props(new AbstractClientDataStructureTestClientDataStructure(dataTypeInstanceId, controlAlgorithm, outgoingConnection = TestProbe().ref)))
      dataType ! ReceiveCallback((_) => {})
      dataType ! CreateResponse(dataTypeInstanceId)
      val data = "{foo}"
      val operation = AbstractClientDataStructureSpecTestOperation(OperationId(), OperationContext(List.empty), ClientId(), data)
      val operationMessage = OperationMessage(
        ClientId(),
        dataTypeInstanceId,
        AbstractClientDataStructureSpec.dataTypeName,
        List(operation)
      )

      dataType ! operationMessage
      dataType ! operationMessage

      dataType.underlyingActor.historyBuffer.history should equal(List(operation))
    }

    "must not apply duplicated received operations with WaveOT" in {
      val dataTypeInstanceId = DataStructureInstanceId()
      //because the control algorithm has to check for duplicates, we have to make sure the data type listens to it
      val dataType: TestActorRef[AbstractClientDataStructureTestClientDataStructure] = TestActorRef(
        Props(new AbstractClientDataStructureTestClientDataStructure(dataTypeInstanceId, new WaveOTClient((op) => {}), outgoingConnection = TestProbe().ref)))
      dataType ! ReceiveCallback((_) => {})
      dataType ! CreateResponse(dataTypeInstanceId)
      val data = "{foo}"
      val operation = AbstractClientDataStructureSpecTestOperation(OperationId(), OperationContext(List.empty), ClientId(), data)
      val operationMessage = OperationMessage(
        ClientId(),
        dataTypeInstanceId,
        AbstractClientDataStructureSpec.dataTypeName,
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
      val dataType: TestActorRef[AbstractClientDataStructureTestClientDataStructure] = TestActorRef(Props(new AbstractClientDataStructureTestClientDataStructure(dataTypeInstanceId, new WaveOTClient(op => outgoing.ref ! op), outgoingConnection = outgoing.ref)))
      dataType ! ReceiveCallback((_) => {})
      val operation = AbstractClientDataStructureSpecTestOperation(OperationId(), OperationContext(List.empty), ClientId(), data)
      val operation2 = AbstractClientDataStructureSpecTestOperation(OperationId(), OperationContext(List.empty), ClientId(), data)
      val operationMessage = OperationMessage(ClientId(), dataTypeInstanceId, AbstractClientDataStructureSpec.dataTypeName, List(operation))
      val operationMessage2 = OperationMessage(ClientId(), dataTypeInstanceId, AbstractClientDataStructureSpec.dataTypeName, List(operation2))

      dataType ! LocalOperationMessage(operationMessage)
      dataType ! LocalOperationMessage(operationMessage2)
      dataType ! CreateResponse(dataTypeInstanceId)

      val toServer = outgoing.expectMsgClass(classOf[AbstractClientDataStructureSpecTestOperation])
    }


    "pass acknowledgements to the control algorithm" in {
      val outgoing = TestProbe()
      val dataTypeInstanceId = DataStructureInstanceId()
      val data = "{foo}"
      val operation = AbstractClientDataStructureSpecTestOperation(OperationId(), OperationContext(List.empty), ClientId(), data)
      val operation2 = AbstractClientDataStructureSpecTestOperation(OperationId(), OperationContext(List.empty), ClientId(), data)
      val operationMessage = OperationMessage(ClientId(), dataTypeInstanceId, AbstractClientDataStructureSpec.dataTypeName, List(operation))
      val operationMessage2 = OperationMessage(ClientId(), dataTypeInstanceId, AbstractClientDataStructureSpec.dataTypeName, List(operation2))
      val controlAlgorithm = new ControlAlgorithmClient {

        var correctOperationPassed = false

        override def canLocalOperationBeApplied(op: DataStructureOperation): Boolean = true

        override def transform(op: DataStructureOperation, history: HistoryBuffer, transformer: OperationTransformer): DataStructureOperation = op

        override def canBeApplied(op: DataStructureOperation, history: HistoryBuffer): Boolean = {
          if (op == operation) correctOperationPassed = true
          true
        }

        override def currentOperationContext: OperationContext = OperationContext(List.empty) // not important here
      }
      val dataType: TestActorRef[AbstractClientDataStructureTestClientDataStructure] = TestActorRef(
        Props(new AbstractClientDataStructureTestClientDataStructure(dataTypeInstanceId, controlAlgorithm, outgoingConnection = outgoing.ref)))
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
      val operation = AbstractClientDataStructureSpecTestOperation(OperationId(), OperationContext(List.empty), ClientId(), data)
      val operation2 = AbstractClientDataStructureSpecTestOperation(OperationId(), OperationContext(List.empty), ClientId(), data)
      val operationMessage = OperationMessage(ClientId(), dataTypeInstanceId, AbstractClientDataStructureSpec.dataTypeName, List(operation))
      val operationMessage2 = OperationMessage(ClientId(), dataTypeInstanceId, AbstractClientDataStructureSpec.dataTypeName, List(operation2))

      val controlAlgorithm = new ControlAlgorithmClient {
        var first = true
        var correct = true

        override def canLocalOperationBeApplied(op: DataStructureOperation): Boolean = {
          if (first) {
            correct &= op == operation
            op should equal(operation)
            first = false
          } else {
            val comparisonOperation = AbstractClientDataStructureSpecTestOperation(operation2.id, OperationContext(List(operation.id)), operation2.clientId, operation2.data)
            correct &= op == comparisonOperation
            op should equal(comparisonOperation)
          }
          true
        }

        override def currentOperationContext: OperationContext = OperationContext(List.empty)

        override def transform(op: DataStructureOperation, history: HistoryBuffer, transformer: OperationTransformer): DataStructureOperation = op

        override def canBeApplied(op: DataStructureOperation, history: HistoryBuffer): Boolean = true
      }

      val dataType: TestActorRef[AbstractClientDataStructureTestClientDataStructure] = TestActorRef(Props(new AbstractClientDataStructureTestClientDataStructure(dataTypeInstanceId, controlAlgorithm, outgoingConnection = outgoing.ref)))
      dataType ! ReceiveCallback((_) => {})

      dataType ! LocalOperationMessage(operationMessage)
      dataType ! LocalOperationMessage(operationMessage2)
      dataType ! CreateResponse(dataTypeInstanceId)

      controlAlgorithm.correct should equal(true)
    }

    "add initialized lastOperationId to first local operation when being unacknowledged" in {
      val lastOperationId = OperationId()
      val dataType: TestActorRef[AbstractClientDataStructureTestClientDataStructure] = TestActorRef(
        Props(new AbstractClientDataStructureTestClientDataStructure(DataStructureInstanceId(), new AbstractClientDataStructureSpecControlAlgorithmClient, Option(lastOperationId), outgoingConnection = TestProbe().ref)))
      dataType ! ReceiveCallback((_) => {})
      val operation = AbstractClientDataStructureSpecTestOperation(OperationId(), OperationContext(List.empty), ClientId(), "")
      val operationMessage = OperationMessage(ClientId(), DataStructureInstanceId(), AbstractClientDataStructureSpec.dataTypeName, List(operation))

      dataType ! LocalOperationMessage(operationMessage)

      dataType.underlyingActor.historyBuffer.history.head should equal(AbstractClientDataStructureSpecTestOperation(operation.id, OperationContext(List(lastOperationId)), operation.clientId, operation.data))
    }

    "add initialized lastOperationId to first local operation when going from un- to acknowledged" in {
      val lastOperationId = OperationId()
      val dataType: TestActorRef[AbstractClientDataStructureTestClientDataStructure] = TestActorRef(
        Props(new AbstractClientDataStructureTestClientDataStructure(DataStructureInstanceId(), new AbstractClientDataStructureSpecControlAlgorithmClient, Option(lastOperationId), outgoingConnection = TestProbe().ref)))
      dataType ! ReceiveCallback((_) => {})
      dataType ! CreateResponse(DataStructureInstanceId())
      val operation = AbstractClientDataStructureSpecTestOperation(OperationId(), OperationContext(List.empty), ClientId(), "")
      val operationMessage = OperationMessage(ClientId(), DataStructureInstanceId(), AbstractClientDataStructureSpec.dataTypeName, List(operation))

      dataType ! LocalOperationMessage(operationMessage)

      dataType.underlyingActor.historyBuffer.history.head should equal(AbstractClientDataStructureSpecTestOperation(operation.id, OperationContext(List(lastOperationId)), operation.clientId, operation.data))
    }

    "add initialized lastOperationId to first local operation when being a remote instantiation" in {
      val lastOperationId = OperationId()
      val dataType: TestActorRef[AbstractClientDataStructureTestClientDataStructure] = TestActorRef(
        Props(new AbstractClientDataStructureTestClientDataStructure(DataStructureInstanceId(), new AbstractClientDataStructureSpecControlAlgorithmClient, Option(lastOperationId), outgoingConnection = TestProbe().ref)))
      dataType ! RemoteInstantiation
      dataType ! ReceiveCallback((_) => {})
      val operation = AbstractClientDataStructureSpecTestOperation(OperationId(), OperationContext(List.empty), ClientId(), "")
      val operationMessage = OperationMessage(ClientId(), DataStructureInstanceId(), AbstractClientDataStructureSpec.dataTypeName, List(operation))

      dataType ! LocalOperationMessage(operationMessage)

      dataType.underlyingActor.historyBuffer.history.head should equal(AbstractClientDataStructureSpecTestOperation(operation.id, OperationContext(List(lastOperationId)), operation.clientId, operation.data))
    }

    "ignore messages until callback is set when being a remote instantiation" in {
      val dataType: TestActorRef[AbstractClientDataStructureTestClientDataStructure] = TestActorRef(
        Props(new AbstractClientDataStructureTestClientDataStructure(DataStructureInstanceId(), new AbstractClientDataStructureSpecControlAlgorithmClient, outgoingConnection = TestProbe().ref)))
      val operationMessage = OperationMessage(
        ClientId(),
        DataStructureInstanceId(),
        AbstractClientDataStructureSpec.dataTypeName,
        List(
          AbstractClientDataStructureSpecTestOperation(OperationId(), OperationContext(List.empty), ClientId(), "")
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
      val dataType: TestActorRef[AbstractClientDataStructureTestClientDataStructure] = TestActorRef(
        Props(new AbstractClientDataStructureTestClientDataStructure(DataStructureInstanceId(), new AbstractClientDataStructureSpecControlAlgorithmClient, Option(lastOperationId), outgoingConnection = TestProbe().ref)))
      dataType ! ReceiveCallback((_) => {})
      dataType ! CreateResponse(DataStructureInstanceId())
      val operation = AbstractClientDataStructureSpecTestOperation(OperationId(), OperationContext(List(lastOperationId)), ClientId(), "")
      val operationMessage = OperationMessage(ClientId(), DataStructureInstanceId(), AbstractClientDataStructureSpec.dataTypeName, List(operation))

      dataType ! operationMessage

      //if the data type ignores the lastOperationId as context of the OperationMessage it'll send a HistoricOperationRequest
      expectNoMsg()
    }

    "not remove the inFlightOperation if it is the second operation within the message before transforming the first one against it with WaveOT" in {
      val dataTypeInstanceId = DataStructureInstanceId()
      val data = "{foo}"
      //create own transformer so the context update happens
      val testTransformer = new OperationTransformer {
        override def transform(pair: (DataStructureOperation, DataStructureOperation)): DataStructureOperation = {
          val operation = pair._1.asInstanceOf[AbstractClientDataStructureSpecTestOperation]
          AbstractClientDataStructureSpecTestOperation(operation.id, OperationContext(List(pair._2.id)), operation.clientId, operation.data)
        }

        override protected def transformInternal(pair: (DataStructureOperation, DataStructureOperation), withNewContext: Boolean): DataStructureOperation = {
          pair._1
        }
      }
      val dataType: TestActorRef[AbstractClientDataStructureTestClientDataStructure] = TestActorRef(Props(new AbstractClientDataStructureTestClientDataStructure(dataTypeInstanceId, new WaveOTClient((op) => {}), outgoingConnection = TestProbe().ref) {
        override val transformer = testTransformer
      }))
      dataType ! ReceiveCallback((_) => {})
      dataType ! CreateResponse(dataTypeInstanceId)
      val localOperation = AbstractClientDataStructureSpecTestOperation(OperationId(), OperationContext(List.empty), ClientId(), data)
      val localOperationMsg = OperationMessage(ClientId(), dataTypeInstanceId, AbstractClientDataStructureSpec.dataTypeName, List(localOperation))
      val additionalOperation = AbstractClientDataStructureSpecTestOperation(OperationId(), OperationContext(List.empty), ClientId(), data)
      val localOperationWithChangedContext = AbstractClientDataStructureSpecTestOperation(localOperation.id, OperationContext(List(additionalOperation.id)), localOperation.clientId, localOperation.data)

      dataType ! LocalOperationMessage(localOperationMsg)

      dataType ! OperationMessage(ClientId(), dataTypeInstanceId, AbstractClientDataStructureSpec.dataTypeName, List(localOperationWithChangedContext, additionalOperation))

      dataType.underlyingActor.historyBuffer.history.find(op => op.id == additionalOperation.id).get.operationContext should equal(OperationContext(List(localOperation.id)))
    }


    "generate a LocalOperationEvent after applying a local operation when unacknowledged" in {
      val dataTypeInstanceId = DataStructureInstanceId()
      val data = "{foo}"
      val latch = new CountDownLatch(1)
      val operation = AbstractClientDataStructureSpecTestOperation(OperationId(), OperationContext(List.empty), ClientId(), data)
      val dataType: TestActorRef[AbstractClientDataStructureTestClientDataStructure] = TestActorRef(
        Props(new AbstractClientDataStructureTestClientDataStructure(dataTypeInstanceId, new AbstractClientDataStructureSpecControlAlgorithmClient, outgoingConnection = TestProbe().ref)))
      dataType ! ReceiveCallback((event: ClientDataStructureEvent) => {
        event.asInstanceOf[LocalOperationEvent].operation should equal(operation)
        latch.countDown()
      })
      val operationMessage = OperationMessage(
        ClientId(),
        dataTypeInstanceId,
        AbstractClientDataStructureSpec.dataTypeName,
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
      val operation = AbstractClientDataStructureSpecTestOperation(OperationId(), OperationContext(List.empty), ClientId(), data)
      val dataType: TestActorRef[AbstractClientDataStructureTestClientDataStructure] = TestActorRef(
        Props(new AbstractClientDataStructureTestClientDataStructure(dataTypeInstanceId, new AbstractClientDataStructureSpecControlAlgorithmClient, outgoingConnection = TestProbe().ref)))
      dataType ! RemoteInstantiation
      dataType ! ReceiveCallback((event: ClientDataStructureEvent) => {
        event.asInstanceOf[LocalOperationEvent].operation should equal(operation)
        latch.countDown()
      })
      val operationMessage = OperationMessage(
        ClientId(),
        dataTypeInstanceId,
        AbstractClientDataStructureSpec.dataTypeName,
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
      val operation = AbstractClientDataStructureSpecTestOperation(OperationId(), OperationContext(List.empty), ClientId(), data)
      val dataType: TestActorRef[AbstractClientDataStructureTestClientDataStructure] = TestActorRef(
        Props(new AbstractClientDataStructureTestClientDataStructure(dataTypeInstanceId, new AbstractClientDataStructureSpecControlAlgorithmClient, outgoingConnection = TestProbe().ref)))
      dataType ! RemoteInstantiation
      dataType ! ReceiveCallback((event: ClientDataStructureEvent) => {
        event.asInstanceOf[RemoteOperationEvent].operation should equal(operation)
        latch.countDown()
      })
      val operationMessage = OperationMessage(
        ClientId(),
        dataTypeInstanceId,
        AbstractClientDataStructureSpec.dataTypeName,
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
      val dataType: TestActorRef[AbstractClientDataStructureTestClientDataStructure] = TestActorRef(
        Props(new AbstractClientDataStructureTestClientDataStructure(dataTypeInstanceId, new AbstractClientDataStructureSpecControlAlgorithmClient, outgoingConnection = TestProbe().ref)))
      dataType ! ReceiveCallback((event: ClientDataStructureEvent) => {
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
      val operation = AbstractClientDataStructureSpecTestOperation(OperationId(), OperationContext(List.empty), ClientId(), data)
      val dataType: TestActorRef[AbstractClientDataStructureTestClientDataStructure] = TestActorRef(
        Props(new AbstractClientDataStructureTestClientDataStructure(dataTypeInstanceId, new AbstractClientDataStructureSpecControlAlgorithmClient(canRemoteBeApplied = false), outgoingConnection = TestProbe().ref)))
      dataType ! RemoteInstantiation
      dataType ! ReceiveCallback((event: ClientDataStructureEvent) => {
        event.asInstanceOf[AcknowledgementEvent].operation should equal(operation)
        latch.countDown()
      })
      val operationMessage = OperationMessage(
        ClientId(),
        dataTypeInstanceId,
        AbstractClientDataStructureSpec.dataTypeName,
        List(operation)
      )

      dataType ! operationMessage

      val awaiting = latch.await(5, TimeUnit.SECONDS)
      awaiting should equal(true)
    }
  }
}

class AbstractClientDataStructureTestClientDataStructure(
                                                dataStructureInstanceId: DataStructureInstanceId,
                                                clientControlAlgorithm: ControlAlgorithmClient,
                                                lastOperationId: Option[OperationId] = Option.empty,
                                                outgoingConnection: ActorRef)
  extends AbstractClientDataStructure(dataStructureInstanceId, clientControlAlgorithm, lastOperationId, outgoingConnection) {

  var data = "{test}"

  override val dataStructureName: DataStructureName = AbstractClientDataStructureSpec.dataTypeName

  override val transformer: OperationTransformer = new OperationTransformer {
    override def transform(pair: (DataStructureOperation, DataStructureOperation)): DataStructureOperation = pair._1

    override def bulkTransform(operation: DataStructureOperation, bridge: List[DataStructureOperation]): List[DataStructureOperation] = bridge

    override protected def transformInternal(pair: (DataStructureOperation, DataStructureOperation), withNewContext: Boolean): DataStructureOperation = pair._1
  }

  override def apply(op: DataStructureOperation): Unit = {
    op match {
      case test: AbstractClientDataStructureSpecTestOperation => data = test.data
      case _ => fail
    }
  }

  override def getDataAsJson: String = data

  override def cloneOperationWithNewContext(op: DataStructureOperation, context: OperationContext): DataStructureOperation = {
    op match {
      case abstr: AbstractClientDataStructureSpecTestOperation => AbstractClientDataStructureSpecTestOperation(abstr.id, context, abstr.clientId, abstr.data)
    }
  }
}

case class AbstractClientDataStructureSpecTestOperation(id: OperationId, operationContext: OperationContext, var clientId: ClientId, data: String) extends DataStructureOperation

object AbstractClientDataStructureSpec {
  val dataTypeName = DataStructureName("AbstractClientDataType")
}

class AbstractClientDataStructureSpecControlAlgorithmClient(canRemoteBeApplied: Boolean = true) extends ControlAlgorithmClient {

  var didTransform = false

  var context: List[OperationId] = List.empty

  override def canLocalOperationBeApplied(op: DataStructureOperation): Boolean = {
    context = List(op.id)
    true
  }

  override def canBeApplied(op: DataStructureOperation, history: HistoryBuffer): Boolean = canRemoteBeApplied

  override def transform(op: DataStructureOperation, history: HistoryBuffer, transformer: OperationTransformer): DataStructureOperation = {
    context = List(op.id)
    didTransform = true
    op
  }

  override def currentOperationContext: OperationContext = {
    OperationContext(context)
  }
}