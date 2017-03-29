package de.tu_berlin.formic.datatype.tree

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestActorRef, TestKit}
import de.tu_berlin.formic.common.controlalgo.ControlAlgorithm
import de.tu_berlin.formic.common.datatype.{DataTypeOperation, HistoryBuffer, OperationContext, OperationTransformer}
import de.tu_berlin.formic.common.message.{OperationMessage, UpdateRequest, UpdateResponse}
import de.tu_berlin.formic.common.{ClientId, DataStructureInstanceId, OperationId}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import upickle.default._

/**
  * @author Ronny Bräunlich
  */
class TreeServerDataTypeSpec extends TestKit(ActorSystem("TreeServerDataTypeSpec"))
  with WordSpecLike
  with ImplicitSender
  with Matchers
  with BeforeAndAfterAll {

  override def afterAll(): Unit = {
    system.terminate()
  }

  implicit val reader = new ValueTreeNodeReader[Boolean]()

  "TreeServerDataType" must {
    "insert data" in {
      val dataTypeInstanceId: DataStructureInstanceId = DataStructureInstanceId()
      val tree = system.actorOf(Props(new TreeServerDataStructure[Boolean](dataTypeInstanceId, TreeServerDataTypeSpecControlAlgorithm, BooleanTreeDataTypeFactory.name)))
      val op1 = TreeInsertOperation(AccessPath(), ValueTreeNode(true, List(ValueTreeNode(false))), OperationId(), OperationContext(), ClientId())
      val op2 = TreeInsertOperation(AccessPath(1), ValueTreeNode(true), OperationId(), OperationContext(), ClientId())
      val op3 = TreeInsertOperation(AccessPath(1, 0), ValueTreeNode(false), OperationId(), OperationContext(), ClientId())

      tree ! OperationMessage(ClientId(), dataTypeInstanceId, BooleanTreeDataTypeFactory.name, List(op1))
      tree ! OperationMessage(ClientId(), dataTypeInstanceId, BooleanTreeDataTypeFactory.name, List(op2))
      tree ! OperationMessage(ClientId(), dataTypeInstanceId, BooleanTreeDataTypeFactory.name, List(op3))

      tree ! UpdateRequest(ClientId(), dataTypeInstanceId)
      read[ValueTreeNode](expectMsgClass(classOf[UpdateResponse]).data) should equal(ValueTreeNode(true, List(ValueTreeNode(false), ValueTreeNode(true, List(ValueTreeNode(false))))))
    }

    "delete data" in {
      val dataTypeInstanceId: DataStructureInstanceId = DataStructureInstanceId()
      val tree = system.actorOf(Props(new TreeServerDataStructure[Boolean](dataTypeInstanceId, TreeServerDataTypeSpecControlAlgorithm, BooleanTreeDataTypeFactory.name)))
      val op1 = TreeInsertOperation(AccessPath(), ValueTreeNode(true, List(ValueTreeNode(false))), OperationId(), OperationContext(), ClientId())
      val op2 = TreeInsertOperation(AccessPath(1), ValueTreeNode(true), OperationId(), OperationContext(), ClientId())
      val op3 = TreeInsertOperation(AccessPath(1, 0), ValueTreeNode(false), OperationId(), OperationContext(), ClientId())
      tree ! OperationMessage(ClientId(), dataTypeInstanceId, BooleanTreeDataTypeFactory.name, List(op1))
      tree ! OperationMessage(ClientId(), dataTypeInstanceId, BooleanTreeDataTypeFactory.name, List(op2))
      tree ! OperationMessage(ClientId(), dataTypeInstanceId, BooleanTreeDataTypeFactory.name, List(op3))
      val deleteOperation = TreeDeleteOperation(AccessPath(1), OperationId(), OperationContext(), ClientId())

      tree ! OperationMessage(ClientId(), dataTypeInstanceId, BooleanTreeDataTypeFactory.name, List(deleteOperation))

      tree ! UpdateRequest(ClientId(), dataTypeInstanceId)
      read[ValueTreeNode](expectMsgClass(classOf[UpdateResponse]).data) should equal(ValueTreeNode(true, List(ValueTreeNode(false))))
    }

    "not change after no-operation" in {
      val dataTypeInstanceId: DataStructureInstanceId = DataStructureInstanceId()
      val tree = system.actorOf(Props(new TreeServerDataStructure[Boolean](dataTypeInstanceId, TreeServerDataTypeSpecControlAlgorithm, BooleanTreeDataTypeFactory.name)))
      val op = TreeInsertOperation(AccessPath(), ValueTreeNode(true, List(ValueTreeNode(false))), OperationId(), OperationContext(), ClientId())
      val noop = TreeNoOperation(OperationId(), OperationContext(), ClientId())
      tree ! OperationMessage(ClientId(), dataTypeInstanceId, BooleanTreeDataTypeFactory.name, List(op))

      tree ! OperationMessage(ClientId(), dataTypeInstanceId, BooleanTreeDataTypeFactory.name, List(noop))

      tree ! UpdateRequest(ClientId(), dataTypeInstanceId)
      read[ValueTreeNode](expectMsgClass(classOf[UpdateResponse]).data) should equal(ValueTreeNode(true, List(ValueTreeNode(false))))
    }

    "result in valid JSON representation" in {
      val dataTypeInstanceId: DataStructureInstanceId = DataStructureInstanceId()
      val tree = system.actorOf(Props(new TreeServerDataStructure[Boolean](dataTypeInstanceId, TreeServerDataTypeSpecControlAlgorithm, BooleanTreeDataTypeFactory.name)))
      val op1 = TreeInsertOperation(AccessPath(), ValueTreeNode(true, List(ValueTreeNode(false))), OperationId(), OperationContext(), ClientId())
      val op2 = TreeInsertOperation(AccessPath(1), ValueTreeNode(true), OperationId(), OperationContext(), ClientId())

      tree ! OperationMessage(ClientId(), dataTypeInstanceId, BooleanTreeDataTypeFactory.name, List(op1))
      tree ! OperationMessage(ClientId(), dataTypeInstanceId, BooleanTreeDataTypeFactory.name, List(op2))

      tree ! UpdateRequest(ClientId(), dataTypeInstanceId)
      expectMsg(UpdateResponse(dataTypeInstanceId, BooleanTreeDataTypeFactory.name, "{\"value\":true,\"children\":[{\"value\":false,\"children\":[]},{\"value\":true,\"children\":[]}]}", Some(op2.id)))
    }
  }
}

object TreeServerDataTypeSpecControlAlgorithm extends ControlAlgorithm {

  override def canBeApplied(op: DataTypeOperation, history: HistoryBuffer): Boolean = true

  override def transform(op: DataTypeOperation, history: HistoryBuffer, transformer: OperationTransformer): DataTypeOperation = op
}
