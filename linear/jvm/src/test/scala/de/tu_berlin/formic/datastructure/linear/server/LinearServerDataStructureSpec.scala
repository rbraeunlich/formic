package de.tu_berlin.formic.datastructure.linear.server

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestActorRef, TestKit}
import de.tu_berlin.formic.common.controlalgo.ControlAlgorithm
import de.tu_berlin.formic.common.datastructure.{DataStructureOperation, HistoryBuffer, OperationContext, OperationTransformer}
import de.tu_berlin.formic.common.message.{OperationMessage, UpdateRequest, UpdateResponse}
import de.tu_berlin.formic.common.{ClientId, DataStructureInstanceId, OperationId}
import de.tu_berlin.formic.datastructure.linear.{LinearDeleteOperation, LinearInsertOperation, LinearNoOperation}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.collection.mutable.ArrayBuffer

/**
  * @author Ronny Bräunlich
  */
class LinearServerDataStructureSpec extends TestKit(ActorSystem("LinearServerDataStructureSpec"))
  with WordSpecLike
  with ImplicitSender
  with Matchers
  with BeforeAndAfterAll {

  override def afterAll(): Unit = {
    system.terminate()
  }

  "LinearDataStructure" must {

    "insert data" in {
      val dataTypeInstanceId: DataStructureInstanceId = DataStructureInstanceId()
      val dataType = system.actorOf(Props(LinearServerDataStructure[Int](dataTypeInstanceId, LinearDataStructureSpecControlAlgorithm, IntegerListDataStructureFactory.name)))
      val op = LinearInsertOperation(0, Integer.valueOf(1), OperationId(), OperationContext(List.empty), ClientId())
      val op2 = LinearInsertOperation(1, Integer.valueOf(3), OperationId(), OperationContext(List.empty), ClientId())
      val op3 = LinearInsertOperation(0, Integer.valueOf(0), OperationId(), OperationContext(List.empty), ClientId())

      dataType ! OperationMessage(ClientId(), DataStructureInstanceId(), IntegerListDataStructureFactory.name, List(op))
      dataType ! OperationMessage(ClientId(), DataStructureInstanceId(), IntegerListDataStructureFactory.name, List(op2))
      dataType ! OperationMessage(ClientId(), DataStructureInstanceId(), IntegerListDataStructureFactory.name, List(op3))

      dataType ! UpdateRequest(ClientId(), dataTypeInstanceId)
      expectMsg(UpdateResponse(dataTypeInstanceId, IntegerListDataStructureFactory.name, "[0,1,3]", Some(op3.id)))
    }

    "delete data" in {
      val dataTypeInstanceId: DataStructureInstanceId = DataStructureInstanceId()
      val dataType = system.actorOf(Props(LinearServerDataStructure[Int](dataTypeInstanceId, LinearDataStructureSpecControlAlgorithm, IntegerListDataStructureFactory.name)))
      val op = LinearInsertOperation(0, Integer.valueOf(1), OperationId(), OperationContext(List.empty), ClientId())
      val op2 = LinearInsertOperation(1, Integer.valueOf(3), OperationId(), OperationContext(List.empty), ClientId())
      val op3 = LinearInsertOperation(0, Integer.valueOf(0), OperationId(), OperationContext(List.empty), ClientId())

      //operations have to be reversed!
      dataType ! OperationMessage(ClientId(), DataStructureInstanceId(), IntegerListDataStructureFactory.name, List(op3, op2, op))

      val delete = LinearDeleteOperation(1, OperationId(), OperationContext(List.empty), ClientId())

      dataType ! OperationMessage(ClientId(), DataStructureInstanceId(), IntegerListDataStructureFactory.name, List(delete))

      dataType ! UpdateRequest(ClientId(), dataTypeInstanceId)
      expectMsg(UpdateResponse(dataTypeInstanceId, IntegerListDataStructureFactory.name, "[0,3]", Some(delete.id)))
    }

    "not change when receiving no-op operation" in {
      val dataTypeInstanceId: DataStructureInstanceId = DataStructureInstanceId()
      val dataType = system.actorOf(Props(LinearServerDataStructure[Int](dataTypeInstanceId, LinearDataStructureSpecControlAlgorithm, IntegerListDataStructureFactory.name)))
      //insert some data
      val op = LinearInsertOperation(0, Integer.valueOf(1), OperationId(), OperationContext(List.empty), ClientId())
      val op2 = LinearInsertOperation(1, Integer.valueOf(3), OperationId(), OperationContext(List.empty), ClientId())
      val noop = LinearNoOperation(OperationId(), OperationContext(List.empty), ClientId())

      dataType ! OperationMessage(ClientId(), DataStructureInstanceId(), IntegerListDataStructureFactory.name, List(op))
      dataType ! OperationMessage(ClientId(), DataStructureInstanceId(), IntegerListDataStructureFactory.name, List(op2))
      dataType ! OperationMessage(ClientId(), DataStructureInstanceId(), IntegerListDataStructureFactory.name, List(noop))

      dataType ! UpdateRequest(ClientId(), dataTypeInstanceId)
      expectMsg(UpdateResponse(dataTypeInstanceId, IntegerListDataStructureFactory.name, "[1,3]", Some(noop.id)))
    }

    "result in a valid JSON representation" in {
      val dataTypeInstanceId: DataStructureInstanceId = DataStructureInstanceId()
      val dataType = system.actorOf(Props(LinearServerDataStructure[Int](dataTypeInstanceId, LinearDataStructureSpecControlAlgorithm, IntegerListDataStructureFactory.name)))
      val op = LinearInsertOperation(0, Integer.valueOf(1), OperationId(), OperationContext(List.empty), ClientId())
      val op2 = LinearInsertOperation(1, Integer.valueOf(3), OperationId(), OperationContext(List.empty), ClientId())
      val op3 = LinearInsertOperation(0, Integer.valueOf(0), OperationId(), OperationContext(List.empty), ClientId())

      //operations have to be reversed!
      dataType ! OperationMessage(ClientId(), DataStructureInstanceId(), IntegerListDataStructureFactory.name, List(op3, op2, op))

      dataType ! UpdateRequest(ClientId(), dataTypeInstanceId)
      expectMsg(UpdateResponse(dataTypeInstanceId, IntegerListDataStructureFactory.name, "[0,1,3]", Some(op3.id)))
    }

  }
}
object LinearDataStructureSpecControlAlgorithm extends ControlAlgorithm {

  override def canBeApplied(op: DataStructureOperation, history: HistoryBuffer): Boolean = true

  override def transform(op: DataStructureOperation, history: HistoryBuffer, transformer: OperationTransformer): DataStructureOperation = op
}