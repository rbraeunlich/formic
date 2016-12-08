package de.tu_berlin.formic.datatype.linear.client

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestActorRef, TestKit}
import de.tu_berlin.formic.common.{ClientId, DataTypeInstanceId, OperationId}
import de.tu_berlin.formic.common.controlalgo.ControlAlgorithmClient
import de.tu_berlin.formic.common.datatype.FormicDataType.LocalOperationMessage
import de.tu_berlin.formic.common.datatype._
import de.tu_berlin.formic.common.datatype.client.AbstractClientDataType.ReceiveCallback
import de.tu_berlin.formic.common.message.OperationMessage
import de.tu_berlin.formic.datatype.linear.{LinearDeleteOperation, LinearInsertOperation}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import upickle.default._

import scala.collection.mutable.ArrayBuffer

/**
  * @author Ronny Bräunlich
  */
class LinearClientDataTypeSpec extends TestKit(ActorSystem("FormicListSpec"))
  with WordSpecLike
  with BeforeAndAfterAll
  with Matchers
  with ImplicitSender {

  override def afterAll(): Unit = {
    system.terminate()
  }

  "LinearClientDataType" must {

    "create an empty array buffer when no initial data is present" in {
      val dataType: TestActorRef[LinearClientDataType[Int]] =
        TestActorRef(Props(new LinearClientDataType[Int](DataTypeInstanceId(), new LinearClientDataTypeSpecControlAlgoClient, DataTypeName("test"), Option.empty)))

      dataType.underlyingActor.data should equal(ArrayBuffer.empty[Int])
    }

    "use the initial data when present" in {
      val initialData = ArrayBuffer(15,6,7,8)
      val initialDataJson = write(initialData)
      val dataType: TestActorRef[LinearClientDataType[Int]] =
        TestActorRef(Props(new LinearClientDataType[Int](DataTypeInstanceId(), new LinearClientDataTypeSpecControlAlgoClient, DataTypeName("test"), Option(initialDataJson))))

      dataType.underlyingActor.data should equal(initialData)
    }

    "apply a linear insert operation correctly" in {
      val dataType: TestActorRef[LinearClientDataType[Int]] =
        TestActorRef(Props(new LinearClientDataType[Int](DataTypeInstanceId(), new LinearClientDataTypeSpecControlAlgoClient, DataTypeName("test"), Option.empty)))
      val op = LinearInsertOperation(0, 213, OperationId(), OperationContext(List.empty), ClientId())
      val opMsg = OperationMessage(ClientId(), DataTypeInstanceId(), DataTypeName("test"), List(op))
      dataType ! ReceiveCallback(() => {})

      dataType ! opMsg

      awaitAssert(dataType.underlyingActor.data should equal(ArrayBuffer(213)))
    }

    "apply a linear delete operation correctly" in {
      val dataType: TestActorRef[LinearClientDataType[Int]] =
        TestActorRef(Props(new LinearClientDataType[Int](DataTypeInstanceId(), new LinearClientDataTypeSpecControlAlgoClient, DataTypeName("test"), Option.empty)))
      val opIns = LinearInsertOperation(0, 213, OperationId(), OperationContext(List.empty), ClientId())
      val opInsMsg = OperationMessage(ClientId(), DataTypeInstanceId(), DataTypeName("test"), List(opIns))
      val opDel = LinearDeleteOperation(0, OperationId(), OperationContext(List.empty), ClientId())
      val opDelMsg = OperationMessage(ClientId(), DataTypeInstanceId(), DataTypeName("test"), List(opDel))
      dataType ! ReceiveCallback(() => {})
      dataType ! opInsMsg

      dataType ! opDelMsg

      awaitAssert(dataType.underlyingActor.data should equal(ArrayBuffer[Int]()))
    }

    "clone a local linear insert operation correctly" in {
      val dataType: TestActorRef[LinearClientDataType[Int]] =
        TestActorRef(Props(new LinearClientDataType[Int](DataTypeInstanceId(), new LinearClientDataTypeSpecControlAlgoClient, DataTypeName("test"), Option.empty)))
      //one operation first to be able to check the changed OperationContext
      val opId = OperationId()
      val opIns = LinearInsertOperation(0, 213, opId, OperationContext(List.empty), ClientId())
      val opInsMsg = OperationMessage(ClientId(), DataTypeInstanceId(), DataTypeName("test"), List(opIns))
      val opInsLocal = LinearInsertOperation(0, 23, OperationId(), OperationContext(List.empty), ClientId())
      val opInsMsgLocal = LocalOperationMessage(OperationMessage(ClientId(), DataTypeInstanceId(), DataTypeName("test"), List(opInsLocal)))
      dataType ! ReceiveCallback(() => {})
      dataType ! opInsMsg

      dataType ! opInsMsgLocal

      awaitAssert(dataType.underlyingActor.historyBuffer.history.head should equal(
        LinearInsertOperation(opInsLocal.index, opInsLocal.o, opInsLocal.id, OperationContext(List(opId)), opInsLocal.clientId)
      ))
    }

    "clone a local linear delete operation correctly" in {
      val dataType: TestActorRef[LinearClientDataType[Int]] =
        TestActorRef(Props(new LinearClientDataType[Int](DataTypeInstanceId(), new LinearClientDataTypeSpecControlAlgoClient, DataTypeName("test"), Option.empty)))
      //one operation first to be able to check the changed OperationContext
      val opId = OperationId()
      val opIns = LinearInsertOperation(0, 213, opId, OperationContext(List.empty), ClientId())
      val opInsMsg = OperationMessage(ClientId(), DataTypeInstanceId(), DataTypeName("test"), List(opIns))
      val opDelLocal = LinearDeleteOperation(0, OperationId(), OperationContext(List.empty), ClientId())
      val opDelMsgLocal = LocalOperationMessage(OperationMessage(ClientId(), DataTypeInstanceId(), DataTypeName("test"), List(opDelLocal)))
      dataType ! ReceiveCallback(() => {})
      dataType ! opInsMsg

      dataType ! opDelMsgLocal

      awaitAssert(dataType.underlyingActor.historyBuffer.history.head should equal(
        LinearDeleteOperation(opDelLocal.index, opDelLocal.id, OperationContext(List(opId)), opDelLocal.clientId)
      ))
    }

    "correctly return its data as JSON" in {
      val dataType: TestActorRef[LinearClientDataType[Int]] =
        TestActorRef(Props(new LinearClientDataType[Int](DataTypeInstanceId(), new LinearClientDataTypeSpecControlAlgoClient, DataTypeName("test"), Option.empty)))
      val opIns = LinearInsertOperation(0, 213, OperationId(), OperationContext(List.empty), ClientId())
      val opInsMsg = OperationMessage(ClientId(), DataTypeInstanceId(), DataTypeName("test"), List(opIns))
      dataType ! ReceiveCallback(() => {})

      dataType ! opInsMsg

      awaitAssert(dataType.underlyingActor.getDataAsJson should equal("[213]"))
    }
  }
}

class LinearClientDataTypeSpecControlAlgoClient extends ControlAlgorithmClient {

  override def canLocalOperationBeApplied(op: DataTypeOperation): Boolean = true

  override def canBeApplied(op: DataTypeOperation, history: HistoryBuffer): Boolean = true

  override def transform(op: DataTypeOperation, history: HistoryBuffer, transformer: OperationTransformer): DataTypeOperation = op
}