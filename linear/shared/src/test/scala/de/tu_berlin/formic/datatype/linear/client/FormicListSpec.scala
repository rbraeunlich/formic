package de.tu_berlin.formic.datatype.linear.client

import akka.actor.ActorSystem
import akka.testkit.{TestKit, TestProbe}
import de.tu_berlin.formic.common.{ClientId, DataStructureInstanceId}
import de.tu_berlin.formic.common.datatype.FormicDataStructure.LocalOperationMessage
import de.tu_berlin.formic.common.datatype.client.AbstractClientDataStructure.ReceiveCallback
import de.tu_berlin.formic.common.datatype.client.{ClientDataTypeEvent, DataStructureInitiator}
import de.tu_berlin.formic.common.datatype.{FormicDataStructure, OperationContext}
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
      val list = new FormicBooleanList((_) => {}, initiator)

      initiator.initCalled should be(true)
    }

    "inform the wrapped data type actor about a new callback when set" in {
      val dataTypeActor = TestProbe()
      val list = new FormicBooleanList((_) => {}, RemoteDataStructureInitiator)
      list.actor = dataTypeActor.ref
      val newCallback: (ClientDataTypeEvent) => Unit = (_) => {println("test")}

      list.callback = newCallback

      dataTypeActor.expectMsg(ReceiveCallback(newCallback))
    }

    "wrap add invocation in LocalOperationMessage and send it to the data type actor" in {
      val dataTypeActor = TestProbe()
      val dataTypeInstanceId = DataStructureInstanceId()
      val list = new FormicBooleanList((_) => {}, RemoteDataStructureInitiator, dataTypeInstanceId)
      list.actor = dataTypeActor.ref
      list.add(0, false)

      val msg = dataTypeActor.expectMsgClass(classOf[LocalOperationMessage])
      val wrappedMsg = msg.op
      wrappedMsg.dataStructureInstanceId should equal(dataTypeInstanceId)
      wrappedMsg.dataStructure should equal(list.dataStructureName)
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
      val dataTypeInstanceId = DataStructureInstanceId()
      val list = new FormicBooleanList((_) => {}, RemoteDataStructureInitiator, dataTypeInstanceId)
      list.actor = dataTypeActor.ref
      list.add(0, false)
      dataTypeActor.receiveN(1)
      list.remove(0)


      val msg = dataTypeActor.expectMsgClass(classOf[LocalOperationMessage])
      val wrappedMsg = msg.op
      wrappedMsg.dataStructureInstanceId should equal(dataTypeInstanceId)
      wrappedMsg.dataStructure should equal(list.dataStructureName)
      wrappedMsg.operations should have size 1
      val insertOperation = wrappedMsg.operations.head.asInstanceOf[LinearDeleteOperation]
      insertOperation.index should be(0)
      insertOperation.clientId should be(null)
      insertOperation.id should not be null
      insertOperation.operationContext should equal(OperationContext(List.empty))
    }

    "send an UpdateRequest to the wrapped data type actor when get is called" in {
      val dataTypeInstanceId = DataStructureInstanceId()
      val dataTypeActor = new TestProbe(system){
        def receiveUpdateRequestAndAnswer() = {
          expectMsgPF(){
            case up:UpdateRequest =>
              up.dataStructureInstanceId should equal(dataTypeInstanceId)
              sender ! UpdateResponse(dataTypeInstanceId, FormicBooleanListDataTypeFactory.name, "[false]", Option.empty)
          }
        }
      }
      val list = new FormicBooleanList((_) => {}, RemoteDataStructureInitiator, dataTypeInstanceId)
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
      val dataTypeInstanceId = DataStructureInstanceId()
      val dataTypeActor = new TestProbe(system){
        def receiveUpdateRequestAndAnswer() = {
          expectMsgPF(){
            case up:UpdateRequest =>
              up.dataStructureInstanceId should equal(dataTypeInstanceId)
              sender ! UpdateResponse(dataTypeInstanceId, FormicBooleanListDataTypeFactory.name, "[false, true]", Option.empty)
          }
        }
      }
      val list = new FormicBooleanList((_) => {}, RemoteDataStructureInitiator, dataTypeInstanceId)
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
      val dataTypeInstanceId = DataStructureInstanceId()
      val clientId = ClientId()

      val dataTypeActor = new TestProbe(system){
        def receiveUpdateRequestAndAnswer() = {
          expectMsgPF(){
            case up:UpdateRequest =>
              up.dataStructureInstanceId should equal(dataTypeInstanceId)
              up.clientId should equal(clientId)
              sender ! UpdateResponse(dataTypeInstanceId, FormicBooleanListDataTypeFactory.name, "[false, true]", Option.empty)
          }
        }
      }
      val list = new FormicBooleanList((_) => {}, RemoteDataStructureInitiator, dataTypeInstanceId, dataTypeActor.ref, clientId)

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
      val dataTypeInstanceId = DataStructureInstanceId()
      val clientId = ClientId()

      val dataTypeActor = new TestProbe(system){
        def receiveUpdateRequestAndAnswer() = {
          expectMsgPF(){
            case up:UpdateRequest =>
              up.dataStructureInstanceId should equal(dataTypeInstanceId)
              up.clientId should equal(clientId)
              sender ! UpdateResponse(dataTypeInstanceId, FormicDoubleListDataTypeFactory.name, "[0.5, 1.2]", Option.empty)
          }
        }
      }
      val list = new FormicDoubleList((_) => {}, RemoteDataStructureInitiator, dataTypeInstanceId, dataTypeActor.ref, clientId)

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
      val dataTypeInstanceId = DataStructureInstanceId()
      val clientId = ClientId()

      val dataTypeActor = new TestProbe(system){
        def receiveUpdateRequestAndAnswer() = {
          expectMsgPF(){
            case up:UpdateRequest =>
              up.dataStructureInstanceId should equal(dataTypeInstanceId)
              up.clientId should equal(clientId)
              sender ! UpdateResponse(dataTypeInstanceId, FormicIntegerListDataTypeFactory.name, "[4, 5]", Option.empty)
          }
        }
      }
      val list = new FormicIntegerList((_) => {}, RemoteDataStructureInitiator, dataTypeInstanceId, dataTypeActor.ref, clientId)

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
      val dataTypeInstanceId = DataStructureInstanceId()
      val clientId = ClientId()

      val dataTypeActor = new TestProbe(system){
        def receiveUpdateRequestAndAnswer() = {
          expectMsgPF(){
            case up:UpdateRequest =>
              up.dataStructureInstanceId should equal(dataTypeInstanceId)
              up.clientId should equal(clientId)
              sender ! UpdateResponse(dataTypeInstanceId, FormicStringDataTypeFactory.name, "[\"a\", \"b\"]", Option.empty)
          }
        }
      }
      val list = new FormicString((_) => {}, RemoteDataStructureInitiator, dataTypeInstanceId, dataTypeActor.ref, clientId)

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

class FormicListSpecInitiator extends DataStructureInitiator {

  var initCalled = false

  override def initDataStructure(dataType: FormicDataStructure): Unit = {
    initCalled = true
  }
}
