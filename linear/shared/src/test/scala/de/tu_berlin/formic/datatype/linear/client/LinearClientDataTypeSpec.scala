package de.tu_berlin.formic.datatype.linear.client

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestActorRef, TestKit, TestProbe}
import de.tu_berlin.formic.common.controlalgo.ControlAlgorithmClient
import de.tu_berlin.formic.common.datatype.FormicDataType.LocalOperationMessage
import de.tu_berlin.formic.common.datatype._
import de.tu_berlin.formic.common.datatype.client.AbstractClientDataStructure.ReceiveCallback
import de.tu_berlin.formic.common.message.{CreateResponse, OperationMessage}
import de.tu_berlin.formic.common.{ClientId, DataStructureInstanceId, OperationId}
import de.tu_berlin.formic.datatype.linear.{LinearDeleteOperation, LinearInsertOperation, LinearNoOperation}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import upickle.default._

import scala.collection.mutable.ArrayBuffer

/**
  * @author Ronny Bräunlich
  */
class LinearClientDataTypeSpec extends TestKit(ActorSystem("LinearClientDataTypeSpec"))
  with WordSpecLike
  with BeforeAndAfterAll
  with Matchers
  with ImplicitSender {

  override def afterAll(): Unit = {
    system.terminate()
  }

  "LinearClientDataType" must {

    "create an empty array buffer when no initial data is present" in {
      val outgoing = TestProbe()
      val dataType: TestActorRef[LinearClientDataStructure[Int]] =
        TestActorRef(Props(new LinearClientDataStructure[Int](DataStructureInstanceId(), new LinearClientDataTypeSpecControlAlgoClient, DataStructureName("test"), Option.empty, Option.empty, outgoing.ref)))

      dataType.underlyingActor.data should equal(ArrayBuffer.empty[Int])
    }

    "use the initial data when present" in {
      val outgoing = TestProbe()
      val initialData = ArrayBuffer(15,6,7,8)
      val initialDataJson = write(initialData)
      val initialOperationId = OperationId()
      val dataType: TestActorRef[LinearClientDataStructure[Int]] =
        TestActorRef(Props(new LinearClientDataStructure[Int](DataStructureInstanceId(), new LinearClientDataTypeSpecControlAlgoClient, DataStructureName("test"), Option(initialDataJson), Option(initialOperationId), outgoing.ref)))

      dataType.underlyingActor.data should equal(initialData)
    }

    "apply a linear insert operation correctly" in {
      val outgoing = TestProbe()
      val dataType: TestActorRef[LinearClientDataStructure[Int]] =
        TestActorRef(Props(new LinearClientDataStructure[Int](DataStructureInstanceId(), new LinearClientDataTypeSpecControlAlgoClient, DataStructureName("test"), Option.empty, Option.empty, outgoing.ref)))
      val op = LinearInsertOperation(0, 213, OperationId(), OperationContext(List.empty), ClientId())
      val opMsg = OperationMessage(ClientId(), DataStructureInstanceId(), DataStructureName("test"), List(op))
      dataType ! ReceiveCallback((_) => {})
      dataType ! CreateResponse(DataStructureInstanceId())

      dataType ! opMsg

      awaitAssert(dataType.underlyingActor.data should equal(ArrayBuffer(213)))
    }

    "apply a linear delete operation correctly" in {
      val outgoing = TestProbe()
      val dataType: TestActorRef[LinearClientDataStructure[Int]] =
        TestActorRef(Props(new LinearClientDataStructure[Int](DataStructureInstanceId(), new LinearClientDataTypeSpecControlAlgoClient, DataStructureName("test"), Option.empty, Option.empty, outgoing.ref)))
      val opIns = LinearInsertOperation(0, 213, OperationId(), OperationContext(List.empty), ClientId())
      val opInsMsg = OperationMessage(ClientId(), DataStructureInstanceId(), DataStructureName("test"), List(opIns))
      val opDel = LinearDeleteOperation(0, OperationId(), OperationContext(List.empty), ClientId())
      val opDelMsg = OperationMessage(ClientId(), DataStructureInstanceId(), DataStructureName("test"), List(opDel))
      dataType ! ReceiveCallback((_) => {})
      dataType ! CreateResponse(DataStructureInstanceId())
      dataType ! opInsMsg

      dataType ! opDelMsg

      awaitAssert(dataType.underlyingActor.data should equal(ArrayBuffer[Int]()))
    }

    "not change after receiving a no-op operation" in {
      val outgoing = TestProbe()
      //gotta watch the Actor, else the test could pass with an exception
      val watcherProbe = TestProbe()
      val dataType: TestActorRef[LinearClientDataStructure[Int]] =
        TestActorRef(Props(new LinearClientDataStructure[Int](DataStructureInstanceId(), new LinearClientDataTypeSpecControlAlgoClient, DataStructureName("test"), Option.empty, Option.empty, outgoing.ref)))
      watcherProbe watch dataType
      //insert some data
      val op = LinearInsertOperation(0, 2, OperationId(), OperationContext(List.empty), ClientId())
      val op2 = LinearInsertOperation(1, 3, OperationId(), OperationContext(List.empty), ClientId())
      val noop = LinearNoOperation(OperationId(), OperationContext(List.empty), ClientId())
      val opMsg = OperationMessage(ClientId(), DataStructureInstanceId(), DataStructureName("test"), List(op2, op))
      val noopMsg = OperationMessage(ClientId(), DataStructureInstanceId(), DataStructureName("test"), List(noop))
      dataType ! ReceiveCallback((_) => {})
      dataType ! CreateResponse(DataStructureInstanceId())
      dataType ! opMsg

      dataType ! noopMsg

      watcherProbe.expectNoMsg()
      awaitAssert(dataType.underlyingActor.data should equal(ArrayBuffer(2, 3)))
    }

    "clone a local linear insert operation correctly" in {
      val outgoing = TestProbe()
      val dataType: TestActorRef[LinearClientDataStructure[Int]] =
        TestActorRef(Props(new LinearClientDataStructure[Int](DataStructureInstanceId(), new LinearClientDataTypeSpecControlAlgoClient, DataStructureName("test"), Option.empty, Option.empty, outgoing.ref)))
      //one operation first to be able to check the changed OperationContext
      val opId = OperationId()
      val opIns = LinearInsertOperation(0, 213, opId, OperationContext(List.empty), ClientId())
      val opInsMsg = OperationMessage(ClientId(), DataStructureInstanceId(), DataStructureName("test"), List(opIns))
      val opInsLocal = LinearInsertOperation(0, 23, OperationId(), OperationContext(List.empty), ClientId())
      val opInsMsgLocal = LocalOperationMessage(OperationMessage(ClientId(), DataStructureInstanceId(), DataStructureName("test"), List(opInsLocal)))
      dataType ! ReceiveCallback((_) => {})
      dataType ! CreateResponse(DataStructureInstanceId())
      dataType ! opInsMsg

      dataType ! opInsMsgLocal

      awaitAssert(dataType.underlyingActor.historyBuffer.history.head should equal(
        LinearInsertOperation(opInsLocal.index, opInsLocal.o, opInsLocal.id, OperationContext(List(opId)), opInsLocal.clientId)
      ))
    }

    "clone a local linear delete operation correctly" in {
      val outgoing = TestProbe()
      val dataType: TestActorRef[LinearClientDataStructure[Int]] =
        TestActorRef(Props(new LinearClientDataStructure[Int](DataStructureInstanceId(), new LinearClientDataTypeSpecControlAlgoClient, DataStructureName("test"), Option.empty, Option.empty, outgoing.ref)))
      //one operation first to be able to check the changed OperationContext
      val opId = OperationId()
      val opIns = LinearInsertOperation(0, 213, opId, OperationContext(List.empty), ClientId())
      val opInsMsg = OperationMessage(ClientId(), DataStructureInstanceId(), DataStructureName("test"), List(opIns))
      val opDelLocal = LinearDeleteOperation(0, OperationId(), OperationContext(List.empty), ClientId())
      val opDelMsgLocal = LocalOperationMessage(OperationMessage(ClientId(), DataStructureInstanceId(), DataStructureName("test"), List(opDelLocal)))
      dataType ! ReceiveCallback((_) => {})
      dataType ! CreateResponse(DataStructureInstanceId())
      dataType ! opInsMsg

      dataType ! opDelMsgLocal

      awaitAssert(dataType.underlyingActor.historyBuffer.history.head should equal(
        LinearDeleteOperation(opDelLocal.index, opDelLocal.id, OperationContext(List(opId)), opDelLocal.clientId)
      ))
    }

    "correctly return its data as JSON" in {
      val outgoing = TestProbe()
      val dataType: TestActorRef[LinearClientDataStructure[Int]] =
        TestActorRef(Props(new LinearClientDataStructure[Int](DataStructureInstanceId(), new LinearClientDataTypeSpecControlAlgoClient, DataStructureName("test"), Option.empty, Option.empty, outgoing.ref)))
      val opIns = LinearInsertOperation(0, 213, OperationId(), OperationContext(List.empty), ClientId())
      val opInsMsg = OperationMessage(ClientId(), DataStructureInstanceId(), DataStructureName("test"), List(opIns))
      dataType ! ReceiveCallback((_) => {})
      dataType ! CreateResponse(DataStructureInstanceId())

      dataType ! opInsMsg

      awaitAssert(dataType.underlyingActor.getDataAsJson should equal("[213]"))
    }
  }
}

class LinearClientDataTypeSpecControlAlgoClient extends ControlAlgorithmClient {

  var context: List[OperationId] = List.empty

  override def canLocalOperationBeApplied(op: DataTypeOperation): Boolean = {
    context = List(op.id)
    true
  }

  override def canBeApplied(op: DataTypeOperation, history: HistoryBuffer): Boolean = true

  override def transform(op: DataTypeOperation, history: HistoryBuffer, transformer: OperationTransformer): DataTypeOperation = {
    context = List(op.id)
    op
  }

  override def currentOperationContext: OperationContext = {
    OperationContext(context)
  }
}