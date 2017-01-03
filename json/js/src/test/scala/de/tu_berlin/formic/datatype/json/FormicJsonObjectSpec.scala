package de.tu_berlin.formic.datatype.json

import akka.actor.ActorSystem
import akka.testkit.{TestKit, TestProbe}
import de.tu_berlin.formic.common.DataTypeInstanceId
import de.tu_berlin.formic.common.datatype.FormicDataType.LocalOperationMessage
import de.tu_berlin.formic.common.datatype.OperationContext
import de.tu_berlin.formic.common.message.{UpdateRequest, UpdateResponse}
import de.tu_berlin.formic.datatype.json.JsonClientDataType.{JsonClientDeleteOperation, JsonClientInsertOperation, JsonClientReplaceOperation}
import de.tu_berlin.formic.datatype.tree._
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.concurrent.Future
import scala.util.{Failure, Success}

/**
  * @author Ronny Bräunlich
  */
class FormicJsonObjectSpec extends TestKit(ActorSystem("FormicTreeSpec"))
  with WordSpecLike
  with BeforeAndAfterAll
  with Matchers {

  override def afterAll(): Unit = {
    system.terminate()
  }

  implicit val ec = system.dispatcher

  "FormicJsonObject" must {

    "wrap insert invocation for number into LocalOperationMessage with NumberNode" in {
      val dataTypeActor = TestProbe()
      val dataTypeInstanceId = DataTypeInstanceId()
      val jsonObject = new FormicJsonObject(() => {}, RemoteDataTypeInitiator, dataTypeInstanceId, dataTypeActor.ref)
      val key = "num"
      val path = JsonPath(key)

      jsonObject.insert(5, path)

      val localOpMsg = dataTypeActor.expectMsgClass(classOf[LocalOperationMessage])
      val opMsg = localOpMsg.op
      opMsg.dataTypeInstanceId should equal(dataTypeInstanceId)
      opMsg.dataType should equal(jsonObject.dataTypeName)
      opMsg.operations should have size 1
      val operation = opMsg.operations.head.asInstanceOf[JsonClientInsertOperation]
      operation.clientId should be(null)
      operation.id should not be null
      operation.operationContext should be(OperationContext())
      operation.path should be(path)
      operation.tree should be(NumberNode(key, 5))
    }

    "wrap insert invocation for string into LocalOperationMessage with StringNode" in {
      val dataTypeActor = TestProbe()
      val dataTypeInstanceId = DataTypeInstanceId()
      val jsonObject = new FormicJsonObject(() => {}, RemoteDataTypeInitiator, dataTypeInstanceId, dataTypeActor.ref)
      val key = "str"
      val path = JsonPath(key)

      jsonObject.insert("abc", path)

      val localOpMsg = dataTypeActor.expectMsgClass(classOf[LocalOperationMessage])
      val opMsg = localOpMsg.op
      opMsg.dataTypeInstanceId should equal(dataTypeInstanceId)
      opMsg.dataType should equal(jsonObject.dataTypeName)
      opMsg.operations should have size 1
      val operation = opMsg.operations.head.asInstanceOf[JsonClientInsertOperation]
      operation.clientId should be(null)
      operation.id should not be null
      operation.operationContext should be(OperationContext())
      operation.path should be(path)
      operation.tree should be(StringNode(key, List(CharacterNode(null, 'a'), CharacterNode(null, 'b'), CharacterNode(null, 'c'))))
    }

    "wrap insert invocation for character into LocalOperationMessage with CharacterNode" in {
      val dataTypeActor = TestProbe()
      val dataTypeInstanceId = DataTypeInstanceId()
      val jsonObject = new FormicJsonObject(() => {}, RemoteDataTypeInitiator, dataTypeInstanceId, dataTypeActor.ref)
      val key = "str"
      val path = JsonPath("str", "0")

      jsonObject.insert('c', path)

      val localOpMsg = dataTypeActor.expectMsgClass(classOf[LocalOperationMessage])
      val opMsg = localOpMsg.op
      opMsg.dataTypeInstanceId should equal(dataTypeInstanceId)
      opMsg.dataType should equal(jsonObject.dataTypeName)
      opMsg.operations should have size 1
      val operation = opMsg.operations.head.asInstanceOf[JsonClientInsertOperation]
      operation.clientId should be(null)
      operation.id should not be null
      operation.operationContext should be(OperationContext())
      operation.path should be(path)
      operation.tree should be(CharacterNode(null, 'c'))
    }

    "wrap insert invocation for boolean into LocalOperationMessage with BooleanNode" in {
      val dataTypeActor = TestProbe()
      val dataTypeInstanceId = DataTypeInstanceId()
      val jsonObject = new FormicJsonObject(() => {}, RemoteDataTypeInitiator, dataTypeInstanceId, dataTypeActor.ref)
      val key = "bool"
      val path = JsonPath(key)

      jsonObject.insert(b = true, path)

      val localOpMsg = dataTypeActor.expectMsgClass(classOf[LocalOperationMessage])
      val opMsg = localOpMsg.op
      opMsg.dataTypeInstanceId should equal(dataTypeInstanceId)
      opMsg.dataType should equal(jsonObject.dataTypeName)
      opMsg.operations should have size 1
      val operation = opMsg.operations.head.asInstanceOf[JsonClientInsertOperation]
      operation.clientId should be(null)
      operation.id should not be null
      operation.operationContext should be(OperationContext())
      operation.path should be(path)
      operation.tree should be(BooleanNode(key, value = true))
    }

    "wrap insert invocation for object into LocalOperationMessage with ObjectNode" in {
      val dataTypeActor = TestProbe()
      val dataTypeInstanceId = DataTypeInstanceId()
      val jsonObject = new FormicJsonObject(() => {}, RemoteDataTypeInitiator, dataTypeInstanceId, dataTypeActor.ref)
      val key = "bool"
      val path = JsonPath(key)

      jsonObject.insert(ObjectToInsert("in"), path)

      val localOpMsg = dataTypeActor.expectMsgClass(classOf[LocalOperationMessage])
      val opMsg = localOpMsg.op
      opMsg.dataTypeInstanceId should equal(dataTypeInstanceId)
      opMsg.dataType should equal(jsonObject.dataTypeName)
      opMsg.operations should have size 1
      val operation = opMsg.operations.head.asInstanceOf[JsonClientInsertOperation]
      operation.clientId should be(null)
      operation.id should not be null
      operation.operationContext should be(OperationContext())
      operation.path should be(path)
      operation.tree should be(ObjectNode(null, List(StringNode("value", List(CharacterNode(null, 'i'), CharacterNode(null, 'n'))))))
    }

    "wrap remove invocation into LocalOperationMessage" in {
      val dataTypeActor = TestProbe()
      val dataTypeInstanceId = DataTypeInstanceId()
      val jsonObject = new FormicJsonObject(() => {}, RemoteDataTypeInitiator, dataTypeInstanceId, dataTypeActor.ref)
      val path = JsonPath("foo")

      jsonObject.remove(path)

      val localOpMsg = dataTypeActor.expectMsgClass(classOf[LocalOperationMessage])
      val opMsg = localOpMsg.op
      opMsg.dataTypeInstanceId should equal(dataTypeInstanceId)
      opMsg.dataType should equal(jsonObject.dataTypeName)
      opMsg.operations should have size 1
      val operation = opMsg.operations.head.asInstanceOf[JsonClientDeleteOperation]
      operation.clientId should be(null)
      operation.id should not be null
      operation.operationContext should be(OperationContext())
      operation.path should be(path)
    }

    "wrap replace invocation for number into LocalOperationMessage with NumberNode" in {
      val dataTypeActor = TestProbe()
      val dataTypeInstanceId = DataTypeInstanceId()
      val jsonObject = new FormicJsonObject(() => {}, RemoteDataTypeInitiator, dataTypeInstanceId, dataTypeActor.ref)
      val key = "num"
      val path = JsonPath(key)

      jsonObject.replace(5, path)

      val localOpMsg = dataTypeActor.expectMsgClass(classOf[LocalOperationMessage])
      val opMsg = localOpMsg.op
      opMsg.dataTypeInstanceId should equal(dataTypeInstanceId)
      opMsg.dataType should equal(jsonObject.dataTypeName)
      opMsg.operations should have size 1
      val operation = opMsg.operations.head.asInstanceOf[JsonClientReplaceOperation]
      operation.clientId should be(null)
      operation.id should not be null
      operation.operationContext should be(OperationContext())
      operation.path should be(path)
      operation.tree should be(NumberNode(key, 5))
    }

    "wrap replace invocation for string into LocalOperationMessage with StringNode" in {
      val dataTypeActor = TestProbe()
      val dataTypeInstanceId = DataTypeInstanceId()
      val jsonObject = new FormicJsonObject(() => {}, RemoteDataTypeInitiator, dataTypeInstanceId, dataTypeActor.ref)
      val key = "str"
      val path = JsonPath(key)

      jsonObject.replace("abc", path)

      val localOpMsg = dataTypeActor.expectMsgClass(classOf[LocalOperationMessage])
      val opMsg = localOpMsg.op
      opMsg.dataTypeInstanceId should equal(dataTypeInstanceId)
      opMsg.dataType should equal(jsonObject.dataTypeName)
      opMsg.operations should have size 1
      val operation = opMsg.operations.head.asInstanceOf[JsonClientReplaceOperation]
      operation.clientId should be(null)
      operation.id should not be null
      operation.operationContext should be(OperationContext())
      operation.path should be(path)
      operation.tree should be(StringNode(key, List(CharacterNode(null, 'a'), CharacterNode(null, 'b'), CharacterNode(null, 'c'))))
    }

    "wrap replace invocation for character into LocalOperationMessage with CharacterNode" in {
      val dataTypeActor = TestProbe()
      val dataTypeInstanceId = DataTypeInstanceId()
      val jsonObject = new FormicJsonObject(() => {}, RemoteDataTypeInitiator, dataTypeInstanceId, dataTypeActor.ref)
      val key = "str"
      val path = JsonPath("str", "0")

      jsonObject.replace('c', path)

      val localOpMsg = dataTypeActor.expectMsgClass(classOf[LocalOperationMessage])
      val opMsg = localOpMsg.op
      opMsg.dataTypeInstanceId should equal(dataTypeInstanceId)
      opMsg.dataType should equal(jsonObject.dataTypeName)
      opMsg.operations should have size 1
      val operation = opMsg.operations.head.asInstanceOf[JsonClientReplaceOperation]
      operation.clientId should be(null)
      operation.id should not be null
      operation.operationContext should be(OperationContext())
      operation.path should be(path)
      operation.tree should be(CharacterNode(null, 'c'))
    }

    "wrap replace invocation for boolean into LocalOperationMessage with BooleanNode" in {
      val dataTypeActor = TestProbe()
      val dataTypeInstanceId = DataTypeInstanceId()
      val jsonObject = new FormicJsonObject(() => {}, RemoteDataTypeInitiator, dataTypeInstanceId, dataTypeActor.ref)
      val key = "bool"
      val path = JsonPath(key)

      jsonObject.replace(b = true, path)

      val localOpMsg = dataTypeActor.expectMsgClass(classOf[LocalOperationMessage])
      val opMsg = localOpMsg.op
      opMsg.dataTypeInstanceId should equal(dataTypeInstanceId)
      opMsg.dataType should equal(jsonObject.dataTypeName)
      opMsg.operations should have size 1
      val operation = opMsg.operations.head.asInstanceOf[JsonClientReplaceOperation]
      operation.clientId should be(null)
      operation.id should not be null
      operation.operationContext should be(OperationContext())
      operation.path should be(path)
      operation.tree should be(BooleanNode(key, value = true))
    }

    "wrap replace invocation for object into LocalOperationMessage with ObjectNode" in {
      val dataTypeActor = TestProbe()
      val dataTypeInstanceId = DataTypeInstanceId()
      val jsonObject = new FormicJsonObject(() => {}, RemoteDataTypeInitiator, dataTypeInstanceId, dataTypeActor.ref)
      val key = "bool"
      val path = JsonPath(key)

      jsonObject.replace(ObjectToInsert("in"), path)

      val localOpMsg = dataTypeActor.expectMsgClass(classOf[LocalOperationMessage])
      val opMsg = localOpMsg.op
      opMsg.dataTypeInstanceId should equal(dataTypeInstanceId)
      opMsg.dataType should equal(jsonObject.dataTypeName)
      opMsg.operations should have size 1
      val operation = opMsg.operations.head.asInstanceOf[JsonClientReplaceOperation]
      operation.clientId should be(null)
      operation.id should not be null
      operation.operationContext should be(OperationContext())
      operation.path should be(path)
      operation.tree should be(ObjectNode(null, List(StringNode("value", List(CharacterNode(null, 'i'), CharacterNode(null, 'n'))))))

    }

    "send an UpdateRequest to the wrapped data type actor when getNodeAt is called" in {
      val dataTypeInstanceId = DataTypeInstanceId()
      val dataTypeActor = new TestProbe(system) {
        def receiveUpdateRequestAndAnswer() = {
          expectMsgPF() {
            case up: UpdateRequest =>
              up.dataTypeInstanceId should equal(dataTypeInstanceId)
              sender ! UpdateResponse(dataTypeInstanceId, FormicJsonObjectFactory.name, "{\"num\":25}", Option.empty)
          }
        }
      }
      val jsonObject = new FormicJsonObject(() => {}, RemoteDataTypeInitiator, dataTypeInstanceId, dataTypeActor.ref)

      val answer = jsonObject.getNodeAt(JsonPath("num"))

      dataTypeActor.receiveUpdateRequestAndAnswer()

      awaitCond(answer.isCompleted)
      answer.value.get match {
        case Success(t) => t should be(NumberNode("num", 25))
        case Failure(ex) => throw ex
      }
    }

    "send an UpdateRequest to the wrapped data type actor when getValueAt is called" in {
      val dataTypeInstanceId = DataTypeInstanceId()
      val dataTypeActor = new TestProbe(system) {
        def receiveUpdateRequestAndAnswer() = {
          expectMsgPF() {
            case up: UpdateRequest =>
              up.dataTypeInstanceId should equal(dataTypeInstanceId)
              sender ! UpdateResponse(dataTypeInstanceId, FormicJsonObjectFactory.name, "{\"num\":25}", Option.empty)
          }
        }
      }
      val jsonObject = new FormicJsonObject(() => {}, RemoteDataTypeInitiator, dataTypeInstanceId, dataTypeActor.ref)

      val answer: Future[Double] = jsonObject.getValueAt(JsonPath("num"))

      dataTypeActor.receiveUpdateRequestAndAnswer()

      awaitCond(answer.isCompleted)
      answer.value.get match {
        case Success(t) => t should be(25)
        case Failure(ex) => throw ex
      }
    }
  }
}

case class ObjectToInsert(value: String)
