package de.tu_berlin.formic.datastructure.tree.client

import akka.actor.ActorSystem
import akka.testkit.{TestKit, TestProbe}
import de.tu_berlin.formic.common.datastructure.FormicDataStructure.LocalOperationMessage
import de.tu_berlin.formic.common.datastructure.OperationContext
import de.tu_berlin.formic.common.message.{UpdateRequest, UpdateResponse}
import de.tu_berlin.formic.common.{ClientId, DataStructureInstanceId}
import de.tu_berlin.formic.datastructure.tree.{AccessPath, TreeDeleteOperation, TreeInsertOperation, ValueTreeNode}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.util.{Failure, Success}

/**
  * @author Ronny Bräunlich
  */
class FormicTreeSpec extends TestKit(ActorSystem("FormicTreeSpec"))
  with WordSpecLike
  with BeforeAndAfterAll
  with Matchers {

  override def afterAll(): Unit = {
    system.terminate()
  }

  implicit val ec = system.dispatcher

  "Formic Tree" must {
    "wrap insert invocation into LocalOperationMessage" in {
      val dataTypeActor = TestProbe()
      val dataTypeInstanceId = DataStructureInstanceId()
      val clientId = ClientId()
      val tree = new FormicIntegerTree((_) => {}, RemoteDataStructureInitiator, dataTypeInstanceId, dataTypeActor.ref, clientId)

      tree.insert(5, AccessPath())

      val localOpMsg = dataTypeActor.expectMsgClass(classOf[LocalOperationMessage])
      val opMsg = localOpMsg.op
      opMsg.dataStructureInstanceId should equal(dataTypeInstanceId)
      opMsg.dataStructure should equal(tree.dataStructureName)
      opMsg.operations should have size 1
      val operation = opMsg.operations.head.asInstanceOf[TreeInsertOperation]
      operation.clientId should be(clientId)
      operation.id should not be null
      operation.operationContext should be(OperationContext())
      operation.accessPath should be(AccessPath())
      operation.tree should be(ValueTreeNode(5))
    }

    "wrap remove invocation into LocalOperationMessage" in {
      val dataTypeActor = TestProbe()
      val dataTypeInstanceId = DataStructureInstanceId()
      val clientId = ClientId()
      val tree = new FormicIntegerTree((_) => {}, RemoteDataStructureInitiator, dataTypeInstanceId, dataTypeActor.ref, clientId)

      tree.remove(AccessPath(0))

      val localOpMsg = dataTypeActor.expectMsgClass(classOf[LocalOperationMessage])
      val opMsg = localOpMsg.op
      opMsg.dataStructureInstanceId should equal(dataTypeInstanceId)
      opMsg.dataStructure should equal(tree.dataStructureName)
      opMsg.operations should have size 1
      val operation = opMsg.operations.head.asInstanceOf[TreeDeleteOperation]
      operation.clientId should be(clientId)
      operation.id should not be null
      operation.operationContext should be(OperationContext())
      operation.accessPath should be(AccessPath(0))
    }

    "send an UpdateRequest to the wrapped data type actor when getSubTree is called" in {
      val dataTypeInstanceId = DataStructureInstanceId()
      val clientId = ClientId()
      val dataTypeActor = new TestProbe(system) {
        def receiveUpdateRequestAndAnswer() = {
          expectMsgPF() {
            case up: UpdateRequest =>
              up.dataStructureInstanceId should equal(dataTypeInstanceId)
              up.clientId should equal(clientId)
              sender ! UpdateResponse(dataTypeInstanceId, FormicIntegerTreeFactory.name, "{\"value\":100, \"children\": [{\"value\":25, \"children\": []}]}", Option.empty)
          }
        }
      }
      val tree = new FormicIntegerTree((_) => {}, RemoteDataStructureInitiator, dataTypeInstanceId, dataTypeActor.ref, clientId)

      val answer = tree.getSubTree(AccessPath(0))

      dataTypeActor.receiveUpdateRequestAndAnswer()

      awaitCond(answer.isCompleted)
      answer.value.get match {
        case Success(t) => t should be(ValueTreeNode(25))
        case Failure(ex) => throw ex
      }
    }

    "send an UpdateRequest to the wrapped data type actor when getTree is called" in {
      val dataTypeInstanceId = DataStructureInstanceId()
      val clientId = ClientId()
      val dataTypeActor = new TestProbe(system) {
        def receiveUpdateRequestAndAnswer() = {
          expectMsgPF() {
            case up: UpdateRequest =>
              up.dataStructureInstanceId should equal(dataTypeInstanceId)
              up.clientId should equal(clientId)
              sender ! UpdateResponse(dataTypeInstanceId, FormicIntegerTreeFactory.name, "{\"value\":100, \"children\": []}", Option.empty)
          }
        }
      }
      val tree = new FormicIntegerTree((_) => {}, RemoteDataStructureInitiator, dataTypeInstanceId, dataTypeActor.ref, clientId)

      val answer = tree.getTree()

      dataTypeActor.receiveUpdateRequestAndAnswer()

      awaitCond(answer.isCompleted)
      answer.value.get match {
        case Success(t) => t should be(ValueTreeNode(100))
        case Failure(ex) => throw ex
      }
    }
  }

  "FormicBooleanTree" must {
    "work with boolean values" in {
      val dataTypeInstanceId = DataStructureInstanceId()
      val dataTypeActor = new TestProbe(system) {
        def receiveUpdateRequestAndAnswer() = {
          expectMsgPF() {
            case up: UpdateRequest =>
              up.dataStructureInstanceId should equal(dataTypeInstanceId)
              sender ! UpdateResponse(dataTypeInstanceId, FormicBooleanTreeFactory.name, "{\"value\":true, \"children\": []}", Option.empty)
          }
        }
      }
      val tree = new FormicBooleanTree((_) => {}, RemoteDataStructureInitiator, dataTypeInstanceId, dataTypeActor.ref, ClientId())

      tree.insert(true, AccessPath(0))
      dataTypeActor.receiveN(1)
      tree.insert(false, AccessPath(1))
      dataTypeActor.receiveN(1)

      val answer = tree.getTree()

      dataTypeActor.receiveUpdateRequestAndAnswer()

      awaitCond(answer.isCompleted)
      answer.value.get match {
        case Success(t) => t should be(ValueTreeNode(true))
        case Failure(ex) => throw ex
      }
    }
  }

  "FormicDoubleTree" must {
    "work with double values" in {
      val dataTypeInstanceId = DataStructureInstanceId()
      val dataTypeActor = new TestProbe(system) {
        def receiveUpdateRequestAndAnswer() = {
          expectMsgPF() {
            case up: UpdateRequest =>
              up.dataStructureInstanceId should equal(dataTypeInstanceId)
              sender ! UpdateResponse(dataTypeInstanceId, FormicDoubleTreeFactory.name, "{\"value\":1.3, \"children\": []}", Option.empty)
          }
        }
      }
      val tree = new FormicDoubleTree((_) => {}, RemoteDataStructureInitiator, dataTypeInstanceId, dataTypeActor.ref, ClientId())

      tree.insert(5.67, AccessPath(0))
      dataTypeActor.receiveN(1)

      val answer = tree.getTree()

      dataTypeActor.receiveUpdateRequestAndAnswer()

      awaitCond(answer.isCompleted)
      answer.value.get match {
        case Success(t) => t should be(ValueTreeNode(1.3))
        case Failure(ex) => throw ex
      }
    }
  }

  "FormicIntegerTree" must {
    "work with integer values" in {
      val dataTypeInstanceId = DataStructureInstanceId()
      val dataTypeActor = new TestProbe(system) {
        def receiveUpdateRequestAndAnswer() = {
          expectMsgPF() {
            case up: UpdateRequest =>
              up.dataStructureInstanceId should equal(dataTypeInstanceId)
              sender ! UpdateResponse(dataTypeInstanceId, FormicIntegerTreeFactory.name, "{\"value\":6, \"children\": []}", Option.empty)
          }
        }
      }
      val tree = new FormicIntegerTree((_) => {}, RemoteDataStructureInitiator, dataTypeInstanceId, dataTypeActor.ref, ClientId())

      tree.insert(9, AccessPath(0))
      dataTypeActor.receiveN(1)

      val answer = tree.getTree()

      dataTypeActor.receiveUpdateRequestAndAnswer()

      awaitCond(answer.isCompleted)
      answer.value.get match {
        case Success(t) => t should be(ValueTreeNode(6))
        case Failure(ex) => throw ex
      }
    }
  }

  "FormicStringTree" must {
    "work with string values" in {
      val dataTypeInstanceId = DataStructureInstanceId()
      val dataTypeActor = new TestProbe(system) {
        def receiveUpdateRequestAndAnswer() = {
          expectMsgPF() {
            case up: UpdateRequest =>
              up.dataStructureInstanceId should equal(dataTypeInstanceId)
              sender ! UpdateResponse(dataTypeInstanceId, FormicStringTreeFactory.name, "{\"value\":\"def\", \"children\": []}", Option.empty)
          }
        }
      }
      val tree = new FormicStringTree((_) => {}, RemoteDataStructureInitiator, dataTypeInstanceId, dataTypeActor.ref, ClientId())

      tree.insert("a", AccessPath(0))
      dataTypeActor.receiveN(1)

      val answer = tree.getTree()

      dataTypeActor.receiveUpdateRequestAndAnswer()

      awaitCond(answer.isCompleted)
      answer.value.get match {
        case Success(t) => t should be(ValueTreeNode("def"))
        case Failure(ex) => throw ex
      }
    }
  }
}
