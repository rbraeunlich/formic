package de.tu_berlin.formic.datatype.tree.client

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestActorRef, TestKit, TestProbe}
import de.tu_berlin.formic.common.controlalgo.ControlAlgorithmClient
import de.tu_berlin.formic.common.datatype.FormicDataStructure.LocalOperationMessage
import de.tu_berlin.formic.common.datatype._
import de.tu_berlin.formic.common.datatype.client.AbstractClientDataStructure.ReceiveCallback
import de.tu_berlin.formic.common.message.{CreateResponse, OperationMessage}
import de.tu_berlin.formic.common.{ClientId, DataStructureInstanceId, OperationId}
import de.tu_berlin.formic.datatype.tree._
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import upickle.default._

/**
  * @author Ronny Bräunlich
  */
class TreeClientDataTypeSpec extends TestKit(ActorSystem("TreeClientDataTypeSpec"))
  with WordSpecLike
  with BeforeAndAfterAll
  with Matchers
  with ImplicitSender {

  implicit val treeNodeWriter = new ValueTreeNodeWriter[Double]()

  override def afterAll(): Unit = {
    system.terminate()
  }

  "TreeClientDataType" must {

    "have empty tree node if no initial data was provided" in {
      val outgoing = TestProbe()
      val dataType: TestActorRef[TreeClientDataStructure[Double]] =
        TestActorRef(Props(new TreeClientDataStructure[Double](DataStructureInstanceId(), new TreeClientDataTypeSpecControlAlgoClient, DataStructureName("test"), Option.empty, Option.empty, outgoing.ref)))

      dataType.underlyingActor.data should equal(EmptyTreeNode)
    }

    "take initial tree data" in {
      val initialTree = ValueTreeNode(1.5, List(ValueTreeNode(2.0), ValueTreeNode(102.8)))
      val initialDataJson = write(initialTree)
      val initialOperationId: OperationId = OperationId()
      val outgoing = TestProbe()
      val dataType: TestActorRef[TreeClientDataStructure[Double]] =
        TestActorRef(Props(new TreeClientDataStructure[Double](DataStructureInstanceId(), new TreeClientDataTypeSpecControlAlgoClient, DataStructureName("test"), Option(initialDataJson), Option(initialOperationId), outgoing.ref)))

      dataType.underlyingActor.data should equal(initialTree)
    }

    "apply insert operation correctly" in {
      val outgoing = TestProbe()
      val tree = ValueTreeNode(1.6, List(ValueTreeNode(2.0), ValueTreeNode(1.7)))
      val dataType: TestActorRef[TreeClientDataStructure[Double]] =
        TestActorRef(Props(new TreeClientDataStructure[Double](DataStructureInstanceId(), new TreeClientDataTypeSpecControlAlgoClient, DataStructureName("test"), Option.empty, Option.empty, outgoing.ref)))
      val operation = TreeInsertOperation(AccessPath(), tree, OperationId(), OperationContext(), ClientId())
      dataType ! ReceiveCallback((_) => {})
      dataType ! CreateResponse(DataStructureInstanceId())

      dataType ! OperationMessage(ClientId(), DataStructureInstanceId(), DataStructureName("Test"), List(operation))

      dataType.underlyingActor.data should equal(tree)
    }

    "apply delete operation correctly" in {
      val outgoing = TestProbe()
      val tree = ValueTreeNode(1.6, List(ValueTreeNode(2.0), ValueTreeNode(1.7)))
      val dataType: TestActorRef[TreeClientDataStructure[Double]] =
        TestActorRef(Props(new TreeClientDataStructure[Double](DataStructureInstanceId(), new TreeClientDataTypeSpecControlAlgoClient, DataStructureName("test"), Option(write(tree)), Option(OperationId()), outgoing.ref)))
      val operation = TreeDeleteOperation(AccessPath(0), OperationId(), OperationContext(), ClientId())
      dataType ! ReceiveCallback((_) => {})
      dataType ! CreateResponse(DataStructureInstanceId())

      dataType ! OperationMessage(ClientId(), DataStructureInstanceId(), DataStructureName("Test"), List(operation))

      dataType.underlyingActor.data should equal(ValueTreeNode(1.6, List(ValueTreeNode(1.7))))
    }

    "not change after receiving no operation" in {
      val outgoing = TestProbe()
      val tree = ValueTreeNode(1.6, List(ValueTreeNode(2.0), ValueTreeNode(1.7)))
      val dataType: TestActorRef[TreeClientDataStructure[Double]] =
        TestActorRef(Props(new TreeClientDataStructure[Double](DataStructureInstanceId(), new TreeClientDataTypeSpecControlAlgoClient, DataStructureName("test"), Option(write(tree)), Option(OperationId()), outgoing.ref)))
      val operation = TreeNoOperation(OperationId(), OperationContext(), ClientId())
      dataType ! ReceiveCallback((_) => {})
      dataType ! CreateResponse(DataStructureInstanceId())

      dataType ! OperationMessage(ClientId(), DataStructureInstanceId(), DataStructureName("Test"), List(operation))

      dataType.underlyingActor.data should equal(tree)
    }

    "correctly return its data as JSON" in {
      val outgoing = TestProbe()
      val tree = ValueTreeNode(100.0, List(ValueTreeNode(1.0), ValueTreeNode(2.0)))
      val dataType: TestActorRef[TreeClientDataStructure[Double]] =
        TestActorRef(Props(new TreeClientDataStructure[Double](DataStructureInstanceId(), new TreeClientDataTypeSpecControlAlgoClient, DataStructureName("test"), Option(write(tree)), Option(OperationId()), outgoing.ref)))

      dataType.underlyingActor.getDataAsJson should equal("{\"value\":100,\"children\":[{\"value\":1,\"children\":[]},{\"value\":2,\"children\":[]}]}")
    }

    "clone an insert operation correctly" in {
      val outgoing = TestProbe()
      val initialOperationId = OperationId()
      val tree = ValueTreeNode(1.6, List(ValueTreeNode(2.0), ValueTreeNode(1.7)))
      val dataType: TestActorRef[TreeClientDataStructure[Double]] =
        TestActorRef(Props(new TreeClientDataStructure[Double](DataStructureInstanceId(), new TreeClientDataTypeSpecControlAlgoClient, DataStructureName("test"), Option(write(tree)), Option(initialOperationId), outgoing.ref)))
      val operation = TreeInsertOperation(AccessPath(2), ValueTreeNode(5.6), OperationId(), OperationContext(), ClientId())
      dataType ! ReceiveCallback((_) => {})

      dataType ! LocalOperationMessage(OperationMessage(ClientId(), DataStructureInstanceId(), DataStructureName("Test"), List(operation)))

      awaitAssert(dataType.underlyingActor.historyBuffer.history.head should equal(
        TreeInsertOperation(operation.accessPath, operation.tree, operation.id, OperationContext(List(initialOperationId)), operation.clientId)
      ))
    }

    "clone a delete operation correctly" in {
      val outgoing = TestProbe()
      val initialOperationId = OperationId()
      val tree = ValueTreeNode(1.6, List(ValueTreeNode(2.0), ValueTreeNode(1.7)))
      val dataType: TestActorRef[TreeClientDataStructure[Double]] =
        TestActorRef(Props(new TreeClientDataStructure[Double](DataStructureInstanceId(), new TreeClientDataTypeSpecControlAlgoClient, DataStructureName("test"), Option(write(tree)), Option(initialOperationId), outgoing.ref)))
      val operation = TreeDeleteOperation(AccessPath(0), OperationId(), OperationContext(), ClientId())
      dataType ! ReceiveCallback((_) => {})

      dataType ! LocalOperationMessage(OperationMessage(ClientId(), DataStructureInstanceId(), DataStructureName("Test"), List(operation)))

      awaitAssert(dataType.underlyingActor.historyBuffer.history.head should equal(
        TreeDeleteOperation(operation.accessPath, operation.id, OperationContext(List(initialOperationId)), operation.clientId)
      ))
    }
  }
}

class TreeClientDataTypeSpecControlAlgoClient extends ControlAlgorithmClient {

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