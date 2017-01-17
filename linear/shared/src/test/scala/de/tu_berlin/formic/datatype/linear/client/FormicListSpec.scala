package de.tu_berlin.formic.datatype.linear.client

import akka.actor.ActorSystem
import akka.testkit.{TestKit, TestProbe}
import de.tu_berlin.formic.common.{ClientId, DataTypeInstanceId}
import de.tu_berlin.formic.common.datatype.FormicDataType.LocalOperationMessage
import de.tu_berlin.formic.common.datatype.client.AbstractClientDataType.ReceiveCallback
import de.tu_berlin.formic.common.datatype.client.DataTypeInitiator
import de.tu_berlin.formic.common.datatype.{FormicDataType, OperationContext}
import de.tu_berlin.formic.common.message.{UpdateRequest, UpdateResponse}
import de.tu_berlin.formic.datatype.linear.{LinearDeleteOperation, LinearInsertOperation}
import org.scalatest._

import scala.util.{Failure, Success}

/**
  * @author Ronny Bräunlich
  */
class FormicListSpec extends TestKit(ActorSystem("FormicListSpec"))
  with WordSpecLike
  with BeforeAndAfterAll
  with Matchers {

  implicit val ec = system.dispatcher

  override def afterAll(): Unit = {
    system.terminate()
  }

  "FormicList" must {
    "use the initiator" in {
      val initiator = new FormicListSpecInitiator()
      val list = new FormicBooleanList(() => {}, initiator)

      initiator.initCalled should be(true)
    }

    "inform the wrapped data type actor about a new callback when set" in {
      val dataTypeActor = TestProbe()
      val list = new FormicBooleanList(() => {}, RemoteDataTypeInitiator)
      list.actor = dataTypeActor.ref
      val newCallback: () => Unit = () => {println("test")}

      list.callback = newCallback

      dataTypeActor.expectMsg(ReceiveCallback(newCallback))
    }

    "wrap add invocation in LocalOperationMessage and send it to the data type actor" in {
      val dataTypeActor = TestProbe()
      val dataTypeInstanceId = DataTypeInstanceId()
      val list = new FormicBooleanList(() => {}, RemoteDataTypeInitiator, dataTypeInstanceId)
      list.actor = dataTypeActor.ref
      list.add(0, false)

      val msg = dataTypeActor.expectMsgClass(classOf[LocalOperationMessage])
      val wrappedMsg = msg.op
      wrappedMsg.dataTypeInstanceId should equal(dataTypeInstanceId)
      wrappedMsg.dataType should equal(list.dataTypeName)
      wrappedMsg.operations should have size 1
      val insertOperation = wrappedMsg.operations.head.asInstanceOf[LinearInsertOperation]
      insertOperation.index should be(0)
      insertOperation.o.asInstanceOf[Boolean] should be(false)
      insertOperation.clientId should be(null)
      insertOperation.id should not be null
      insertOperation.operationContext should equal(OperationContext(List.empty))
    }

    "wrap remove invocation in LocalOperationMessage and send it to the data type actor" in {
      val dataTypeActor = TestProbe()
      val dataTypeInstanceId = DataTypeInstanceId()
      val list = new FormicBooleanList(() => {}, RemoteDataTypeInitiator, dataTypeInstanceId)
      list.actor = dataTypeActor.ref
      list.add(0, false)
      dataTypeActor.receiveN(1)
      list.remove(0)


      val msg = dataTypeActor.expectMsgClass(classOf[LocalOperationMessage])
      val wrappedMsg = msg.op
      wrappedMsg.dataTypeInstanceId should equal(dataTypeInstanceId)
      wrappedMsg.dataType should equal(list.dataTypeName)
      wrappedMsg.operations should have size 1
      val insertOperation = wrappedMsg.operations.head.asInstanceOf[LinearDeleteOperation]
      insertOperation.index should be(0)
      insertOperation.clientId should be(null)
      insertOperation.id should not be null
      insertOperation.operationContext should equal(OperationContext(List.empty))
    }

    "send an UpdateRequest to the wrapped data type actor when get is called" in {
      val dataTypeInstanceId = DataTypeInstanceId()
      val dataTypeActor = new TestProbe(system){
        def receiveUpdateRequestAndAnswer() = {
          expectMsgPF(){
            case up:UpdateRequest =>
              up.dataTypeInstanceId should equal(dataTypeInstanceId)
              sender ! UpdateResponse(dataTypeInstanceId, FormicBooleanListDataTypeFactory.name, "[false]", Option.empty)
          }
        }
      }
      val list = new FormicBooleanList(() => {}, RemoteDataTypeInitiator, dataTypeInstanceId)
      list.actor = dataTypeActor.ref

      val answer = list.get(0)

      dataTypeActor.receiveUpdateRequestAndAnswer()

      awaitCond(answer.isCompleted)
      answer.value.get match {
        case Success(bool) => bool should be(false)
        case Failure(ex) => fail(ex)
      }
    }

    "send an UpdateRequest to the wrapped data type actor when getAll is called" in {
      val dataTypeInstanceId = DataTypeInstanceId()
      val dataTypeActor = new TestProbe(system){
        def receiveUpdateRequestAndAnswer() = {
          expectMsgPF(){
            case up:UpdateRequest =>
              up.dataTypeInstanceId should equal(dataTypeInstanceId)
              sender ! UpdateResponse(dataTypeInstanceId, FormicBooleanListDataTypeFactory.name, "[false, true]", Option.empty)
          }
        }
      }
      val list = new FormicBooleanList(() => {}, RemoteDataTypeInitiator, dataTypeInstanceId)
      list.actor = dataTypeActor.ref

      val answer = list.getAll()

      dataTypeActor.receiveUpdateRequestAndAnswer()

      awaitCond(answer.isCompleted)
      answer.value.get match {
        case Success(bool) => bool should contain inOrder(false, true)
        case Failure(ex) => fail(ex)
      }
    }
  }

  "FormicBooleanList" must {
    "work with boolean values" in {
      val dataTypeInstanceId = DataTypeInstanceId()
      val clientId = ClientId()

      val dataTypeActor = new TestProbe(system){
        def receiveUpdateRequestAndAnswer() = {
          expectMsgPF(){
            case up:UpdateRequest =>
              up.dataTypeInstanceId should equal(dataTypeInstanceId)
              up.clientId should equal(clientId)
              sender ! UpdateResponse(dataTypeInstanceId, FormicBooleanListDataTypeFactory.name, "[false, true]", Option.empty)
          }
        }
      }
      val list = new FormicBooleanList(() => {}, RemoteDataTypeInitiator, dataTypeInstanceId, dataTypeActor.ref, clientId)

      list.add(0, true)
      dataTypeActor.receiveN(1)
      list.add(1, false)
      dataTypeActor.receiveN(1)

      val answer = list.getAll()

      dataTypeActor.receiveUpdateRequestAndAnswer()

      awaitCond(answer.isCompleted)
      answer.value.get match {
        case Success(bool) => bool should contain inOrder(false, true)
        case Failure(ex) => fail(ex)
      }
    }
  }

  "FormicDoubleList" must {
    "work with double values" in {
      val dataTypeInstanceId = DataTypeInstanceId()
      val clientId = ClientId()

      val dataTypeActor = new TestProbe(system){
        def receiveUpdateRequestAndAnswer() = {
          expectMsgPF(){
            case up:UpdateRequest =>
              up.dataTypeInstanceId should equal(dataTypeInstanceId)
              up.clientId should equal(clientId)
              sender ! UpdateResponse(dataTypeInstanceId, FormicDoubleListDataTypeFactory.name, "[0.5, 1.2]", Option.empty)
          }
        }
      }
      val list = new FormicDoubleList(() => {}, RemoteDataTypeInitiator, dataTypeInstanceId, dataTypeActor.ref, clientId)

      list.add(0, 0.456)
      dataTypeActor.receiveN(1)
      list.add(1, 100.1)
      dataTypeActor.receiveN(1)

      val answer = list.getAll()

      dataTypeActor.receiveUpdateRequestAndAnswer()

      awaitCond(answer.isCompleted)
      answer.value.get match {
        case Success(bool) => bool should contain inOrder(0.5, 1.2)
        case Failure(ex) => fail(ex)
      }
    }
  }

  "FormicIntegerList" must {
    "work with integer values" in {
      val dataTypeInstanceId = DataTypeInstanceId()
      val clientId = ClientId()

      val dataTypeActor = new TestProbe(system){
        def receiveUpdateRequestAndAnswer() = {
          expectMsgPF(){
            case up:UpdateRequest =>
              up.dataTypeInstanceId should equal(dataTypeInstanceId)
              up.clientId should equal(clientId)
              sender ! UpdateResponse(dataTypeInstanceId, FormicIntegerListDataTypeFactory.name, "[4, 5]", Option.empty)
          }
        }
      }
      val list = new FormicIntegerList(() => {}, RemoteDataTypeInitiator, dataTypeInstanceId, dataTypeActor.ref, clientId)

      list.add(0, 4)
      dataTypeActor.receiveN(1)
      list.add(1, 5)
      dataTypeActor.receiveN(1)

      val answer = list.getAll()

      dataTypeActor.receiveUpdateRequestAndAnswer()

      awaitCond(answer.isCompleted)
      answer.value.get match {
        case Success(bool) => bool should contain inOrder(4, 5)
        case Failure(ex) => fail(ex)
      }
    }
  }

  "FormicString" must {
    "work with char values" in {
      val dataTypeInstanceId = DataTypeInstanceId()
      val clientId = ClientId()

      val dataTypeActor = new TestProbe(system){
        def receiveUpdateRequestAndAnswer() = {
          expectMsgPF(){
            case up:UpdateRequest =>
              up.dataTypeInstanceId should equal(dataTypeInstanceId)
              up.clientId should equal(clientId)
              sender ! UpdateResponse(dataTypeInstanceId, FormicStringDataTypeFactory.name, "[\"a\", \"b\"]", Option.empty)
          }
        }
      }
      val list = new FormicString(() => {}, RemoteDataTypeInitiator, dataTypeInstanceId, dataTypeActor.ref, clientId)

      list.add(0, 'a')
      dataTypeActor.receiveN(1)
      list.add(1, 'b')
      dataTypeActor.receiveN(1)

      val answer = list.getAll()

      dataTypeActor.receiveUpdateRequestAndAnswer()

      awaitCond(answer.isCompleted)
      answer.value.get match {
        case Success(bool) => bool should contain inOrder('a', 'b')
        case Failure(ex) => fail(ex)
      }
    }
  }
}

class FormicListSpecInitiator extends DataTypeInitiator {

  var initCalled = false

  override def initDataType(dataType: FormicDataType): Unit = {
    initCalled = true
  }
}
