package de.tu_berlin.formic.datatype.linear.server

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestActorRef, TestKit}
import de.tu_berlin.formic.common.controlalgo.ControlAlgorithm
import de.tu_berlin.formic.common.datatype.{DataTypeOperation, HistoryBuffer, OperationContext, OperationTransformer}
import de.tu_berlin.formic.common.message.{OperationMessage, UpdateRequest, UpdateResponse}
import de.tu_berlin.formic.common.{ClientId, DataStructureInstanceId, OperationId}
import de.tu_berlin.formic.datatype.linear.{LinearDeleteOperation, LinearInsertOperation, LinearNoOperation}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.collection.mutable.ArrayBuffer

/**
  * @author Ronny Bräunlich
  */
class LinearServerDataTypeSpec extends TestKit(ActorSystem("LinearServerDataTypeSpec"))
  with WordSpecLike
  with ImplicitSender
  with Matchers
  with BeforeAndAfterAll {

  override def afterAll(): Unit = {
    system.terminate()
  }

  "LinearDataType" must {

    "insert data" in {
      val dataTypeInstanceId: DataStructureInstanceId = DataStructureInstanceId()
      val dataType = system.actorOf(Props(LinearServerDataStructure[Int](dataTypeInstanceId, LinearDataTypeSpecControlAlgorithm, IntegerListDataTypeFactory.name)))
      val op = LinearInsertOperation(0, Integer.valueOf(1), OperationId(), OperationContext(List.empty), ClientId())
      val op2 = LinearInsertOperation(1, Integer.valueOf(3), OperationId(), OperationContext(List.empty), ClientId())
      val op3 = LinearInsertOperation(0, Integer.valueOf(0), OperationId(), OperationContext(List.empty), ClientId())

      dataType ! OperationMessage(ClientId(), DataStructureInstanceId(), IntegerListDataTypeFactory.name, List(op))
      dataType ! OperationMessage(ClientId(), DataStructureInstanceId(), IntegerListDataTypeFactory.name, List(op2))
      dataType ! OperationMessage(ClientId(), DataStructureInstanceId(), IntegerListDataTypeFactory.name, List(op3))

      dataType ! UpdateRequest(ClientId(), dataTypeInstanceId)
      expectMsg(UpdateResponse(dataTypeInstanceId, IntegerListDataTypeFactory.name, "[0,1,3]", Some(op3.id)))
    }

    "delete data" in {
      val dataTypeInstanceId: DataStructureInstanceId = DataStructureInstanceId()
      val dataType = system.actorOf(Props(LinearServerDataStructure[Int](dataTypeInstanceId, LinearDataTypeSpecControlAlgorithm, IntegerListDataTypeFactory.name)))
      val op = LinearInsertOperation(0, Integer.valueOf(1), OperationId(), OperationContext(List.empty), ClientId())
      val op2 = LinearInsertOperation(1, Integer.valueOf(3), OperationId(), OperationContext(List.empty), ClientId())
      val op3 = LinearInsertOperation(0, Integer.valueOf(0), OperationId(), OperationContext(List.empty), ClientId())

      //operations have to be reversed!
      dataType ! OperationMessage(ClientId(), DataStructureInstanceId(), IntegerListDataTypeFactory.name, List(op3, op2, op))

      val delete = LinearDeleteOperation(1, OperationId(), OperationContext(List.empty), ClientId())

      dataType ! OperationMessage(ClientId(), DataStructureInstanceId(), IntegerListDataTypeFactory.name, List(delete))

      dataType ! UpdateRequest(ClientId(), dataTypeInstanceId)
      expectMsg(UpdateResponse(dataTypeInstanceId, IntegerListDataTypeFactory.name, "[0,3]", Some(delete.id)))
    }

    "not change when receiving no-op operation" in {
      val dataTypeInstanceId: DataStructureInstanceId = DataStructureInstanceId()
      val dataType = system.actorOf(Props(LinearServerDataStructure[Int](dataTypeInstanceId, LinearDataTypeSpecControlAlgorithm, IntegerListDataTypeFactory.name)))
      //insert some data
      val op = LinearInsertOperation(0, Integer.valueOf(1), OperationId(), OperationContext(List.empty), ClientId())
      val op2 = LinearInsertOperation(1, Integer.valueOf(3), OperationId(), OperationContext(List.empty), ClientId())
      val noop = LinearNoOperation(OperationId(), OperationContext(List.empty), ClientId())

      dataType ! OperationMessage(ClientId(), DataStructureInstanceId(), IntegerListDataTypeFactory.name, List(op))
      dataType ! OperationMessage(ClientId(), DataStructureInstanceId(), IntegerListDataTypeFactory.name, List(op2))
      dataType ! OperationMessage(ClientId(), DataStructureInstanceId(), IntegerListDataTypeFactory.name, List(noop))

      dataType ! UpdateRequest(ClientId(), dataTypeInstanceId)
      expectMsg(UpdateResponse(dataTypeInstanceId, IntegerListDataTypeFactory.name, "[1,3]", Some(noop.id)))
    }

    "result in a valid JSON representation" in {
      val dataTypeInstanceId: DataStructureInstanceId = DataStructureInstanceId()
      val dataType = system.actorOf(Props(LinearServerDataStructure[Int](dataTypeInstanceId, LinearDataTypeSpecControlAlgorithm, IntegerListDataTypeFactory.name)))
      val op = LinearInsertOperation(0, Integer.valueOf(1), OperationId(), OperationContext(List.empty), ClientId())
      val op2 = LinearInsertOperation(1, Integer.valueOf(3), OperationId(), OperationContext(List.empty), ClientId())
      val op3 = LinearInsertOperation(0, Integer.valueOf(0), OperationId(), OperationContext(List.empty), ClientId())

      //operations have to be reversed!
      dataType ! OperationMessage(ClientId(), DataStructureInstanceId(), IntegerListDataTypeFactory.name, List(op3, op2, op))

      dataType ! UpdateRequest(ClientId(), dataTypeInstanceId)
      expectMsg(UpdateResponse(dataTypeInstanceId, IntegerListDataTypeFactory.name, "[0,1,3]", Some(op3.id)))
    }

  }
}
object LinearDataTypeSpecControlAlgorithm extends ControlAlgorithm {

  override def canBeApplied(op: DataTypeOperation, history: HistoryBuffer): Boolean = true

  override def transform(op: DataTypeOperation, history: HistoryBuffer, transformer: OperationTransformer): DataTypeOperation = op
}