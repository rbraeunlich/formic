package de.tu_berlin.formic.datatype.json

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestActorRef, TestKit}
import de.tu_berlin.formic.common.controlalgo.ControlAlgorithm
import de.tu_berlin.formic.common.datatype.{DataTypeOperation, HistoryBuffer, OperationContext, OperationTransformer}
import de.tu_berlin.formic.common.message.OperationMessage
import de.tu_berlin.formic.common.{ClientId, DataTypeInstanceId, OperationId}
import de.tu_berlin.formic.datatype.tree.{AccessPath, TreeDeleteOperation, TreeInsertOperation, TreeNoOperation}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}


/**
  * @author Ronny Bräunlich
  */
class JsonServerDataTypeSpec extends TestKit(ActorSystem("TreeServerDataTypeSpec"))
  with WordSpecLike
  with ImplicitSender
  with Matchers
  with BeforeAndAfterAll {

  override def afterAll(): Unit = {
    system.terminate()
  }

  "A Json server data type" must {
    "insert data" in {
      val json: TestActorRef[JsonServerDataType] = TestActorRef(Props(JsonServerDataType(DataTypeInstanceId(), JsonServerDataTypeSpecControlAlgorithm, JsonDataTypeFactory.name)))
      val node1 = StringNode("text", List(CharacterNode("0", 'a'), CharacterNode("1", 'b')))
      val node2 = NumberNode("num", 1)
      val node3 = BooleanNode("true?", value = true)
      val op1 = TreeInsertOperation(AccessPath(0), node1, OperationId(), OperationContext(), ClientId())
      val op2 = TreeInsertOperation(AccessPath(1), node2, OperationId(), OperationContext(), ClientId())
      val op3 = TreeInsertOperation(AccessPath(0), node3, OperationId(), OperationContext(), ClientId())

      json ! OperationMessage(ClientId(), DataTypeInstanceId(), JsonDataTypeFactory.name, List(op1))
      json ! OperationMessage(ClientId(), DataTypeInstanceId(), JsonDataTypeFactory.name, List(op2))
      json ! OperationMessage(ClientId(), DataTypeInstanceId(), JsonDataTypeFactory.name, List(op3))

      json.underlyingActor.data should equal(ObjectNode(null, List(node2, node1, node3)))
    }

    "delete data" in {
      val json: TestActorRef[JsonServerDataType] = TestActorRef(Props(JsonServerDataType(DataTypeInstanceId(), JsonServerDataTypeSpecControlAlgorithm, JsonDataTypeFactory.name)))
      val node1 = StringNode("text", List(CharacterNode(null, 'a'), CharacterNode(null, 'b')))
      val node2 = NumberNode("num", 1)
      val op1 = TreeInsertOperation(AccessPath(0), node1, OperationId(), OperationContext(), ClientId())
      val op2 = TreeInsertOperation(AccessPath(1), node2, OperationId(), OperationContext(), ClientId())
      val deletion = TreeDeleteOperation(AccessPath(1, 0), OperationId(), OperationContext(), ClientId())
      json ! OperationMessage(ClientId(), DataTypeInstanceId(), JsonDataTypeFactory.name, List(op1))
      json ! OperationMessage(ClientId(), DataTypeInstanceId(), JsonDataTypeFactory.name, List(op2))

      json ! OperationMessage(ClientId(), DataTypeInstanceId(), JsonDataTypeFactory.name, List(deletion))

      json.underlyingActor.data should equal(ObjectNode(null, List(StringNode("text", List(CharacterNode(null, 'b'))), node2)))
    }

    "replace data" in {
      val json: TestActorRef[JsonServerDataType] = TestActorRef(Props(JsonServerDataType(DataTypeInstanceId(), JsonServerDataTypeSpecControlAlgorithm, JsonDataTypeFactory.name)))
      val node1 = StringNode("text", List(CharacterNode(null, 'a'), CharacterNode(null, 'b')))
      val op1 = TreeInsertOperation(AccessPath(0), node1, OperationId(), OperationContext(), ClientId())
      val replacement = CharacterNode(null, 'z')
      val replacementOp = JsonReplaceOperation(AccessPath(0, 1), replacement, OperationId(), OperationContext(), ClientId())
      json ! OperationMessage(ClientId(), DataTypeInstanceId(), JsonDataTypeFactory.name, List(op1))

      json ! OperationMessage(ClientId(), DataTypeInstanceId(), JsonDataTypeFactory.name, List(replacementOp))

      json.underlyingActor.data should equal(ObjectNode(null, List(StringNode("text", List(CharacterNode(null, 'a'), replacement)))))
    }

    "not change after no operation" in {
      val json: TestActorRef[JsonServerDataType] = TestActorRef(Props(JsonServerDataType(DataTypeInstanceId(), JsonServerDataTypeSpecControlAlgorithm, JsonDataTypeFactory.name)))
      val node1 = StringNode("text", List(CharacterNode(null, 'a'), CharacterNode(null, 'b')))
      val op1 = TreeInsertOperation(AccessPath(0), node1, OperationId(), OperationContext(), ClientId())
      json ! OperationMessage(ClientId(), DataTypeInstanceId(), JsonDataTypeFactory.name, List(op1))

      json ! TreeNoOperation(OperationId(), OperationContext(), ClientId())

      json.underlyingActor.data should equal(ObjectNode(null, List(node1)))
    }

    "result in valid JSON representation" in {
      val json: TestActorRef[JsonServerDataType] = TestActorRef(Props(JsonServerDataType(DataTypeInstanceId(), JsonServerDataTypeSpecControlAlgorithm, JsonDataTypeFactory.name)))
      val node = ObjectNode("node", List(
        BooleanNode("bool", value = true ),
        NumberNode("num", 1), StringNode("str",
          List(CharacterNode(null, 'a'), CharacterNode(null, 'b'))
        ),
        ArrayNode("arr", List(NumberNode(null, 2.0)))
      ))
      val op = TreeInsertOperation(AccessPath(0), node, OperationId(), OperationContext(), ClientId())
      json ! OperationMessage(ClientId(), DataTypeInstanceId(), JsonDataTypeFactory.name, List(op))

      json.underlyingActor.getDataAsJson should equal("{\"node\":{\"arr\":[2],\"bool\":true,\"num\":1,\"str\":\"ab\"}}")
    }
  }

}

object JsonServerDataTypeSpecControlAlgorithm extends ControlAlgorithm {

  override def canBeApplied(op: DataTypeOperation, history: HistoryBuffer): Boolean = true

  override def transform(op: DataTypeOperation, history: HistoryBuffer, transformer: OperationTransformer): DataTypeOperation = op
}