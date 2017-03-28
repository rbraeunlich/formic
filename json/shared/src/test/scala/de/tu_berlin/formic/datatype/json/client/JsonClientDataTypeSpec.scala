package de.tu_berlin.formic.datatype.json.client

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestActorRef, TestKit, TestProbe}
import de.tu_berlin.formic.common.controlalgo.ControlAlgorithmClient
import de.tu_berlin.formic.common.datatype.FormicDataType.LocalOperationMessage
import de.tu_berlin.formic.common.datatype._
import de.tu_berlin.formic.common.datatype.client.AbstractClientDataType.ReceiveCallback
import de.tu_berlin.formic.common.message.{CreateResponse, OperationMessage}
import de.tu_berlin.formic.common.{ClientId, DataStructureInstanceId$, OperationId}
import de.tu_berlin.formic.datatype.json.JsonFormicJsonDataTypeProtocol._
import de.tu_berlin.formic.datatype.json._
import de.tu_berlin.formic.datatype.tree.{AccessPath, TreeDeleteOperation, TreeInsertOperation, TreeNoOperation}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import upickle.default._
import de.tu_berlin.formic.datatype.json.client.JsonClientDataType._

/**
  * @author Ronny Bräunlich
  */
class JsonClientDataTypeSpec extends TestKit(ActorSystem("TreeClientDataTypeSpec"))
  with WordSpecLike
  with BeforeAndAfterAll
  with Matchers
  with ImplicitSender {

  override def afterAll(): Unit = {
    system.terminate()
  }

  "JsonClientDataType" must {
    "have empty object node when no initial data is present" in {
      val outgoing = TestProbe()
      val dataType: TestActorRef[JsonClientDataType] = TestActorRef(Props(JsonClientDataType(DataStructureInstanceId(), new JsonClientDataTypeSpecControlAlgoClient, DataTypeName("test"), Option.empty, Option.empty, outgoing.ref)))

      dataType.underlyingActor.data should equal(ObjectNode(null, List.empty))
    }

    "take initial JSON data" in {
      val initial = ObjectNode(null, List(BooleanNode("bool", value = true), NumberNode("num", 1.0)))
      val initialOperationId: OperationId = OperationId()
      val outgoing = TestProbe()
      val dataType: TestActorRef[JsonClientDataType] = TestActorRef(Props(JsonClientDataType(DataStructureInstanceId(), new JsonClientDataTypeSpecControlAlgoClient, DataTypeName("test"), Some(write(initial)), Option(initialOperationId), outgoing.ref)))

      dataType.underlyingActor.data should equal(initial)
    }

    "return its data as correct JSON" in {
      val initial = ObjectNode(null, List(BooleanNode("bool", value = true), NumberNode("num", 1.0)))
      val initialOperationId: OperationId = OperationId()
      val outgoing = TestProbe()
      val dataType: TestActorRef[JsonClientDataType] = TestActorRef(Props(JsonClientDataType(DataStructureInstanceId(), new JsonClientDataTypeSpecControlAlgoClient, DataTypeName("test"), Some(write(initial)), Option(initialOperationId), outgoing.ref)))

      dataType.underlyingActor.getDataAsJson should equal("""{"bool":true,"num":1}""")
    }

    "apply remote insert operation" in {
      val outgoing = TestProbe()
      val dataType: TestActorRef[JsonClientDataType] = TestActorRef(Props(JsonClientDataType(DataStructureInstanceId(), new JsonClientDataTypeSpecControlAlgoClient, DataTypeName("test"), Option.empty, Option.empty, outgoing.ref)))
      val toInsert = NumberNode("insert", 2.13)
      val operation = TreeInsertOperation(AccessPath(0), toInsert, OperationId(), OperationContext(), ClientId())
      dataType ! ReceiveCallback((_) => {})
      dataType ! CreateResponse(DataStructureInstanceId())

      dataType ! OperationMessage(ClientId(), DataStructureInstanceId(), DataTypeName("test"), List(operation))

      dataType.underlyingActor.data should equal(ObjectNode(null, List(toInsert)))
    }

    "apply remote remove operation" in {
      val initial = ObjectNode(null, List(BooleanNode("bool", value = true), NumberNode("num", 1.0)))
      val initialOperationId: OperationId = OperationId()
      val outgoing = TestProbe()
      val dataType: TestActorRef[JsonClientDataType] = TestActorRef(Props(JsonClientDataType(DataStructureInstanceId(), new JsonClientDataTypeSpecControlAlgoClient, DataTypeName("test"), Some(write(initial)), Option(initialOperationId), outgoing.ref)))
      val operation = TreeDeleteOperation(AccessPath(1), OperationId(), OperationContext(), ClientId())
      dataType ! ReceiveCallback((_) => {})
      dataType ! CreateResponse(DataStructureInstanceId())

      dataType ! OperationMessage(ClientId(), DataStructureInstanceId(), DataTypeName("test"), List(operation))

      dataType.underlyingActor.data should equal(ObjectNode(null, List(BooleanNode("bool", value = true))))
    }

    "apply remote replace operation" in {
      val initial = ObjectNode(null, List(BooleanNode("bool", value = true), NumberNode("num", 1.0)))
      val initialOperationId: OperationId = OperationId()
      val outgoing = TestProbe()
      val dataType: TestActorRef[JsonClientDataType] = TestActorRef(Props(JsonClientDataType(DataStructureInstanceId(), new JsonClientDataTypeSpecControlAlgoClient, DataTypeName("test"), Some(write(initial)), Option(initialOperationId), outgoing.ref)))
      val replacement = NumberNode("num", 5.0)
      val operation = JsonReplaceOperation(AccessPath(1), replacement, OperationId(), OperationContext(), ClientId())
      dataType ! ReceiveCallback((_) => {})
      dataType ! CreateResponse(DataStructureInstanceId())

      dataType ! OperationMessage(ClientId(), DataStructureInstanceId(), DataTypeName("test"), List(operation))

      dataType.underlyingActor.data should equal(ObjectNode(null, List(BooleanNode("bool", value = true), replacement)))

    }

    "not change after no operation" in {
      val initial = ObjectNode(null, List(BooleanNode("bool", value = true), NumberNode("num", 1.0)))
      val initialOperationId: OperationId = OperationId()
      val outgoing = TestProbe()
      val dataType: TestActorRef[JsonClientDataType] = TestActorRef(Props(JsonClientDataType(DataStructureInstanceId(), new JsonClientDataTypeSpecControlAlgoClient, DataTypeName("test"), Some(write(initial)), Option(initialOperationId), outgoing.ref)))
      val operation = TreeNoOperation(OperationId(), OperationContext(), ClientId())
      dataType ! ReceiveCallback((_) => {})
      dataType ! CreateResponse(DataStructureInstanceId())

      dataType ! OperationMessage(ClientId(), DataStructureInstanceId(), DataTypeName("test"), List(operation))

      dataType.underlyingActor.data should equal(initial)

    }

    "clone an insert operation" in {
      val outgoing = TestProbe()
      val initialOperationId = OperationId()
      val dataType: TestActorRef[JsonClientDataType] = TestActorRef(Props(JsonClientDataType(DataStructureInstanceId(), new JsonClientDataTypeSpecControlAlgoClient, DataTypeName("test"), Option.empty, Some(initialOperationId), outgoing.ref)))
      val toInsert = NumberNode("insert", 2.13)
      val operation = TreeInsertOperation(AccessPath(0), toInsert, OperationId(), OperationContext(), ClientId())
      dataType ! ReceiveCallback((_) => {})

      dataType ! LocalOperationMessage(OperationMessage(ClientId(), DataStructureInstanceId(), DataTypeName("test"), List(operation)))

      dataType.underlyingActor.historyBuffer.history.head should equal(
        TreeInsertOperation(operation.accessPath, toInsert, operation.id, OperationContext(List(initialOperationId)), operation.clientId))
    }

    "clone a delete operation" in {
      val outgoing = TestProbe()
      val initialOperationId = OperationId()
      val initialData = ObjectNode(null, List(NumberNode("num", 3)))
      val dataType: TestActorRef[JsonClientDataType] = TestActorRef(Props(JsonClientDataType(DataStructureInstanceId(), new JsonClientDataTypeSpecControlAlgoClient, DataTypeName("test"), Some(write(initialData)), Some(initialOperationId), outgoing.ref)))
      val operation = TreeDeleteOperation(AccessPath(0), OperationId(), OperationContext(), ClientId())
      dataType ! ReceiveCallback((_) => {})

      dataType ! LocalOperationMessage(OperationMessage(ClientId(), DataStructureInstanceId(), DataTypeName("test"), List(operation)))

      dataType.underlyingActor.historyBuffer.history.head should equal(
        TreeDeleteOperation(operation.accessPath, operation.id, OperationContext(List(initialOperationId)), operation.clientId))
    }

    "clone a replace operation" in {
      val outgoing = TestProbe()
      val initialOperationId = OperationId()
      val initialData = ObjectNode(null, List(NumberNode("num", 3)))
      val dataType: TestActorRef[JsonClientDataType] = TestActorRef(Props(JsonClientDataType(DataStructureInstanceId(), new JsonClientDataTypeSpecControlAlgoClient, DataTypeName("test"), Some(write(initialData)), Some(initialOperationId), outgoing.ref)))
      val replacement = NumberNode("num", 2.13)
      val operation = JsonReplaceOperation(AccessPath(0), replacement, OperationId(), OperationContext(), ClientId())
      dataType ! ReceiveCallback((_) => {})

      dataType ! LocalOperationMessage(OperationMessage(ClientId(), DataStructureInstanceId(), DataTypeName("test"), List(operation)))

      dataType.underlyingActor.historyBuffer.history.head should equal(
        JsonReplaceOperation(operation.accessPath, replacement, operation.id, OperationContext(List(initialOperationId)), operation.clientId))

    }

    "apply local insert operation when being unacknowledged" in {
      val outgoing = TestProbe()
      val dataType: TestActorRef[JsonClientDataType] = TestActorRef(Props(JsonClientDataType(DataStructureInstanceId(), new JsonClientDataTypeSpecControlAlgoClient, DataTypeName("test"), Option.empty, Option.empty, outgoing.ref)))
      val toInsert = NumberNode("insert", 2.13)
      val operation = JsonClientInsertOperation(JsonPath(toInsert.key), toInsert, OperationId(), OperationContext(), ClientId())
      dataType ! ReceiveCallback((_) => {})

      dataType ! LocalOperationMessage(OperationMessage(ClientId(), DataStructureInstanceId(), DataTypeName("test"), List(operation)))

      dataType.underlyingActor.data should equal(ObjectNode(null, List(toInsert)))
    }

    "apply local insert operation when being acknowledged" in {
      val outgoing = TestProbe()
      val dataType: TestActorRef[JsonClientDataType] = TestActorRef(Props(JsonClientDataType(DataStructureInstanceId(), new JsonClientDataTypeSpecControlAlgoClient, DataTypeName("test"), Option.empty, Option.empty, outgoing.ref)))
      val toInsert = NumberNode("insert", 2.13)
      val operation = JsonClientInsertOperation(JsonPath(toInsert.key), toInsert, OperationId(), OperationContext(), ClientId())
      dataType ! ReceiveCallback((_) => {})
      dataType ! CreateResponse(DataStructureInstanceId())

      dataType ! LocalOperationMessage(OperationMessage(ClientId(), DataStructureInstanceId(), DataTypeName("test"), List(operation)))

      dataType.underlyingActor.data should equal(ObjectNode(null, List(toInsert)))
    }

    "apply local remove operation when being unacknowledged" in {
      val outgoing = TestProbe()
      val initialOperationId = OperationId()
      val initialData = ObjectNode(null, List(NumberNode("num", 3)))
      val dataType: TestActorRef[JsonClientDataType] = TestActorRef(Props(JsonClientDataType(DataStructureInstanceId(), new JsonClientDataTypeSpecControlAlgoClient, DataTypeName("test"), Some(write(initialData)), Some(initialOperationId), outgoing.ref)))
      val operation = JsonClientDeleteOperation(JsonPath("num"), OperationId(), OperationContext(), ClientId())
      dataType ! ReceiveCallback((_) => {})

      dataType ! LocalOperationMessage(OperationMessage(ClientId(), DataStructureInstanceId(), DataTypeName("test"), List(operation)))

      dataType.underlyingActor.data should equal(ObjectNode(null, List.empty))
    }

    "apply local remove operation when being acknowledged" in {
      val outgoing = TestProbe()
      val initialOperationId = OperationId()
      val initialData = ObjectNode(null, List(NumberNode("num", 3)))
      val dataType: TestActorRef[JsonClientDataType] = TestActorRef(Props(JsonClientDataType(DataStructureInstanceId(), new JsonClientDataTypeSpecControlAlgoClient, DataTypeName("test"), Some(write(initialData)), Some(initialOperationId), outgoing.ref)))
      val operation = JsonClientDeleteOperation(JsonPath("num"), OperationId(), OperationContext(), ClientId())
      dataType ! ReceiveCallback((_) => {})
      dataType ! CreateResponse(DataStructureInstanceId())

      dataType ! LocalOperationMessage(OperationMessage(ClientId(), DataStructureInstanceId(), DataTypeName("test"), List(operation)))

      dataType.underlyingActor.data should equal(ObjectNode(null, List.empty))
    }

    "apply local replace operation when being unacknowledged" in {
      val outgoing = TestProbe()
      val initialOperationId = OperationId()
      val initialData = ObjectNode(null, List(NumberNode("num", 3)))
      val replacement = NumberNode("num", 4)
      val dataType: TestActorRef[JsonClientDataType] = TestActorRef(Props(JsonClientDataType(DataStructureInstanceId(), new JsonClientDataTypeSpecControlAlgoClient, DataTypeName("test"), Some(write(initialData)), Some(initialOperationId), outgoing.ref)))
      val operation = JsonClientReplaceOperation(JsonPath("num"), replacement, OperationId(), OperationContext(), ClientId())
      dataType ! ReceiveCallback((_) => {})

      dataType ! LocalOperationMessage(OperationMessage(ClientId(), DataStructureInstanceId(), DataTypeName("test"), List(operation)))

      dataType.underlyingActor.data should equal(ObjectNode(null, List(replacement)))
    }

    "apply local replace operation when being acknowledged" in {
      val outgoing = TestProbe()
      val initialOperationId = OperationId()
      val initialData = ObjectNode(null, List(NumberNode("num", 3)))
      val replacement = NumberNode("num", 4)
      val dataType: TestActorRef[JsonClientDataType] = TestActorRef(Props(JsonClientDataType(DataStructureInstanceId(), new JsonClientDataTypeSpecControlAlgoClient, DataTypeName("test"), Some(write(initialData)), Some(initialOperationId), outgoing.ref)))
      val operation = JsonClientReplaceOperation(JsonPath("num"), replacement, OperationId(), OperationContext(), ClientId())
      dataType ! ReceiveCallback((_) => {})
      dataType ! CreateResponse(DataStructureInstanceId())

      dataType ! LocalOperationMessage(OperationMessage(ClientId(), DataStructureInstanceId(), DataTypeName("test"), List(operation)))

      dataType.underlyingActor.data should equal(ObjectNode(null, List(replacement)))
    }

  }

}

class JsonClientDataTypeSpecControlAlgoClient extends ControlAlgorithmClient {

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