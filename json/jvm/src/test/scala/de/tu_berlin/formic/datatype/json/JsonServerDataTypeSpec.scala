package de.tu_berlin.formic.datatype.json

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import de.tu_berlin.formic.common.controlalgo.ControlAlgorithm
import de.tu_berlin.formic.common.datatype.{DataTypeOperation, HistoryBuffer, OperationContext, OperationTransformer}
import de.tu_berlin.formic.common.message.{OperationMessage, UpdateRequest, UpdateResponse}
import de.tu_berlin.formic.common.{ClientId, DataTypeInstanceId, OperationId}
import de.tu_berlin.formic.datatype.json.JsonFormicJsonDataTypeProtocol._
import de.tu_berlin.formic.datatype.tree.{AccessPath, TreeDeleteOperation, TreeInsertOperation, TreeNoOperation}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import upickle.default._

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
      val dataTypeInstanceId: DataTypeInstanceId = DataTypeInstanceId()
      val json = system.actorOf(Props(JsonServerDataType(dataTypeInstanceId, JsonServerDataTypeSpecControlAlgorithm, JsonDataTypeFactory.name)))
      val node1 = StringNode("text", List(CharacterNode(null, 'a'), CharacterNode(null, 'b')))
      val node2 = NumberNode("num", 1)
      val node3 = BooleanNode("true?", value = true)
      val op1 = TreeInsertOperation(AccessPath(0), node1, OperationId(), OperationContext(), ClientId())
      val op2 = TreeInsertOperation(AccessPath(1), node2, OperationId(), OperationContext(), ClientId())
      val op3 = TreeInsertOperation(AccessPath(0), node3, OperationId(), OperationContext(), ClientId())

      json ! OperationMessage(ClientId(), dataTypeInstanceId, JsonDataTypeFactory.name, List(op1))
      json ! OperationMessage(ClientId(), dataTypeInstanceId, JsonDataTypeFactory.name, List(op2))
      json ! OperationMessage(ClientId(), dataTypeInstanceId, JsonDataTypeFactory.name, List(op3))

      json ! UpdateRequest(ClientId(), dataTypeInstanceId)
      read[ObjectNode](expectMsgClass(classOf[UpdateResponse]).data) should equal(ObjectNode(null, List(node2, node1, node3)))
    }

    "delete data" in {
      val dataTypeInstanceId: DataTypeInstanceId = DataTypeInstanceId()
      val json = system.actorOf(Props(JsonServerDataType(dataTypeInstanceId, JsonServerDataTypeSpecControlAlgorithm, JsonDataTypeFactory.name)))
      val node1 = StringNode("text", List(CharacterNode(null, 'a'), CharacterNode(null, 'b')))
      val node2 = NumberNode("num", 1)
      val op1 = TreeInsertOperation(AccessPath(0), node1, OperationId(), OperationContext(), ClientId())
      val op2 = TreeInsertOperation(AccessPath(1), node2, OperationId(), OperationContext(), ClientId())
      val deletion = TreeDeleteOperation(AccessPath(1, 0), OperationId(), OperationContext(), ClientId())
      json ! OperationMessage(ClientId(), dataTypeInstanceId, JsonDataTypeFactory.name, List(op1))
      json ! OperationMessage(ClientId(), dataTypeInstanceId, JsonDataTypeFactory.name, List(op2))

      json ! OperationMessage(ClientId(), dataTypeInstanceId, JsonDataTypeFactory.name, List(deletion))

      json ! UpdateRequest(ClientId(), dataTypeInstanceId)
      read[ObjectNode](expectMsgClass(classOf[UpdateResponse]).data) should equal(ObjectNode(null, List(StringNode("text", List(CharacterNode(null, 'b'))), node2)))
    }

    "replace data" in {
      val dataTypeInstanceId: DataTypeInstanceId = DataTypeInstanceId()
      val json = system.actorOf(Props(JsonServerDataType(dataTypeInstanceId, JsonServerDataTypeSpecControlAlgorithm, JsonDataTypeFactory.name)))
      val node1 = StringNode("text", List(CharacterNode(null, 'a'), CharacterNode(null, 'b')))
      val op1 = TreeInsertOperation(AccessPath(0), node1, OperationId(), OperationContext(), ClientId())
      val replacement = CharacterNode(null, 'z')
      val replacementOp = JsonReplaceOperation(AccessPath(0, 1), replacement, OperationId(), OperationContext(), ClientId())
      json ! OperationMessage(ClientId(), dataTypeInstanceId, JsonDataTypeFactory.name, List(op1))

      json ! OperationMessage(ClientId(), dataTypeInstanceId, JsonDataTypeFactory.name, List(replacementOp))

      json ! UpdateRequest(ClientId(), dataTypeInstanceId)
      read[ObjectNode](expectMsgClass(classOf[UpdateResponse]).data) should equal(ObjectNode(null, List(StringNode("text", List(CharacterNode(null, 'a'), replacement)))))
    }

    "not change after no operation" in {
      val dataTypeInstanceId: DataTypeInstanceId = DataTypeInstanceId()
      val json = system.actorOf(Props(JsonServerDataType(dataTypeInstanceId, JsonServerDataTypeSpecControlAlgorithm, JsonDataTypeFactory.name)))
      val node1 = StringNode("text", List(CharacterNode(null, 'a'), CharacterNode(null, 'b')))
      val op1 = TreeInsertOperation(AccessPath(0), node1, OperationId(), OperationContext(), ClientId())
      json ! OperationMessage(ClientId(), dataTypeInstanceId, JsonDataTypeFactory.name, List(op1))

      json ! TreeNoOperation(OperationId(), OperationContext(), ClientId())

      json ! UpdateRequest(ClientId(), dataTypeInstanceId)
      read[ObjectNode](expectMsgClass(classOf[UpdateResponse]).data) should equal(ObjectNode(null, List(node1)))
    }

    "result in valid JSON representation" in {
      val dataTypeInstanceId: DataTypeInstanceId = DataTypeInstanceId()
      val json = system.actorOf(Props(JsonServerDataType(dataTypeInstanceId, JsonServerDataTypeSpecControlAlgorithm, JsonDataTypeFactory.name)))
      val node = ObjectNode("node", List(
        BooleanNode("bool", value = true),
        NumberNode("num", 1), StringNode("str",
          List(CharacterNode(null, 'a'), CharacterNode(null, 'b'))
        ),
        ArrayNode("arr", List(NumberNode(null, 2.0)))
      ))
      val op = TreeInsertOperation(AccessPath(0), node, OperationId(), OperationContext(), ClientId())
      json ! OperationMessage(ClientId(), dataTypeInstanceId, JsonDataTypeFactory.name, List(op))
      json ! UpdateRequest(ClientId(), dataTypeInstanceId)
      expectMsg(UpdateResponse(dataTypeInstanceId, JsonDataTypeFactory.name, "{\"node\":{\"arr\":[2],\"bool\":true,\"num\":1,\"str\":\"ab\"}}", Some(op.id)))
    }
  }

}

object JsonServerDataTypeSpecControlAlgorithm extends ControlAlgorithm {

  override def canBeApplied(op: DataTypeOperation, history: HistoryBuffer): Boolean = true

  override def transform(op: DataTypeOperation, history: HistoryBuffer, transformer: OperationTransformer): DataTypeOperation = op
}