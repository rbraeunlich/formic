package de.tu_berlin.formic.datatype.json

import de.tu_berlin.formic.common.datatype.{DataTypeName, OperationContext}
import de.tu_berlin.formic.common.{ClientId, OperationId}
import de.tu_berlin.formic.datatype.tree._
import org.scalatest.{FlatSpec, Matchers}
import upickle.default._
import de.tu_berlin.formic.datatype.json.JsonFormicJsonDataTypeProtocol._

/**
  * @author Ronny Bräunlich
  */
class JsonFormicJsonDataTypeProtocolSpec extends FlatSpec with Matchers {

  "A JsonTreeNodeWriter" should "serialize a NumberNode within an Objectnode" in {
    val writer = new JsonTreeNodeWriter
    val node = ObjectNode(null, List(NumberNode("num", 3012.12)))

    write(writer.write0.apply(node)) should equal("{\"num\":3012.12}")
  }

  it should "serialize a false BooleanNode within an ObjectNode" in {
    val writer = new JsonTreeNodeWriter
    val node = ObjectNode(null, List(BooleanNode("bool", value = false)))

    write(writer.write0.apply(node)) should equal("{\"bool\":false}")
  }

  it should "serialize a true BooleanNode within an ObjectNode" in {
    val writer = new JsonTreeNodeWriter
    val node = ObjectNode(null, List(BooleanNode("bool", value = true)))

    write(writer.write0.apply(node)) should equal("{\"bool\":true}")
  }

  it should "serialize a StringNode within an ObjectNode" in {
    val writer = new JsonTreeNodeWriter
    val node = ObjectNode(null, List(StringNode("str", List(CharacterNode(null, 'a'), CharacterNode(null, 'b')))))

    write(writer.write0.apply(node)) should equal("{\"str\":\"ab\"}")
  }

  it should "serialize an ArrayNode" in {
    val writer = new JsonTreeNodeWriter
    val node = ObjectNode(null, List(ArrayNode("arr", List(StringNode(null, List(CharacterNode(null, 'z'))), NumberNode(null, 5), BooleanNode(null, value = false)))))

    write(writer.write0.apply(node)) should equal("{\"arr\":[\"z\",5,false]}")
  }

  it should "serialize a nested ObjectNode" in {
    val writer = new JsonTreeNodeWriter
    val node = ObjectNode(null, List(ObjectNode("nested", List(NumberNode("num", 6)))))

    write(writer.write0.apply(node)) should equal("{\"nested\":{\"num\":6}}")
  }

  "A JsonTreeNodeReader" should "deserialize a number" in {
    val reader = new JsonTreeNodeReader
    val json = "{\"foo\":3.12}"

    read(json)(reader) should equal(ObjectNode(null, List(NumberNode("foo", 3.12))))
  }

  it should "deserialize a true" in {
    val reader = new JsonTreeNodeReader
    val json = "{\"bar\":true}"

    read(json)(reader) should equal(ObjectNode(null, List(BooleanNode("bar", value = true))))
  }

  it should "deserialize a false" in {
    val reader = new JsonTreeNodeReader
    val json = "{\"bar\":false}"

    read(json)(reader) should equal(ObjectNode(null, List(BooleanNode("bar", value = false))))
  }

  it should "deserialize a string" in {
    val reader = new JsonTreeNodeReader
    val json = "{\"text\":\"foo\"}"

    read(json)(reader) should equal(ObjectNode(null, List(StringNode("text", List(CharacterNode(null, 'f'), CharacterNode(null, 'o'), CharacterNode(null, 'o'))))))
  }

  it should "deserialize an array" in {
    val reader = new JsonTreeNodeReader
    val json = "{\"arr\":[\"z\",5,false]}"

    read(json)(reader) should equal(ObjectNode(null, List(ArrayNode("arr", List(StringNode(null, List(CharacterNode(null, 'z'))), NumberNode(null, 5), BooleanNode(null, value = false))))))
  }

  it should "deserialize a nested object" in {
    val reader = new JsonTreeNodeReader
    val json = "{\"nested\":{\"num\":6}}"

    read(json)(reader) should equal(ObjectNode(null, List(ObjectNode("nested", List(NumberNode("num", 6))))))
  }

  "JsonFormicJsonDataTypeProtocol" should "serialize an insert operation" in {
    val operationId = OperationId()
    val clientId = ClientId()
    val protocol = new JsonFormicJsonDataTypeProtocol(DataTypeName("json"))
    val operation = TreeInsertOperation(AccessPath(0, 1), ObjectNode(null, List(NumberNode("foo", 5.2))), operationId, OperationContext(), clientId)

    val serialized = protocol.serializeOperation(operation)

    serialized should equal(s"""{\"accessPath\":[0,1],\"object\":{\"foo\":5.2},\"operationId\":\"${operationId.id}\",\"operationContext\":[],\"clientId\":\"${clientId.id}\"}""")
  }

  it should "serialize a delete operation" in {
    val operationId = OperationId()
    val clientId = ClientId()
    val protocol = new JsonFormicJsonDataTypeProtocol(DataTypeName("json"))
    val operation = TreeDeleteOperation(AccessPath(0), operationId, OperationContext(), clientId)

    val serialized = protocol.serializeOperation(operation)

    serialized should equal(s"""{\"accessPath\":[0],\"operationId\":\"${operationId.id}\",\"operationContext\":[],\"clientId\":\"${clientId.id}\"}""")
  }

  it should "serialize a no operation" in {
    val operationId = OperationId()
    val clientId = ClientId()
    val protocol = new JsonFormicJsonDataTypeProtocol(DataTypeName("json"))
    val operation = TreeNoOperation(operationId, OperationContext(), clientId)

    val serialized = protocol.serializeOperation(operation)

    serialized should equal(s"""{\"accessPath\":[-1],\"operationId\":\"${operationId.id}\",\"operationContext\":[],\"clientId\":\"${clientId.id}\"}""")
  }

  it should "serialize a replace operation" in {
    val operationId = OperationId()
    val clientId = ClientId()
    val protocol = new JsonFormicJsonDataTypeProtocol(DataTypeName("json"))
    val operation = JsonReplaceOperation(AccessPath(0, 1), NumberNode("foo", 5.2), operationId, OperationContext(), clientId)

    val serialized = protocol.serializeOperation(operation)

    serialized should equal(s"""{\"type\":\"replace\",\"accessPath\":[0,1],\"object\":{\"foo\":5.2},\"operationId\":\"${operationId.id}\",\"operationContext\":[],\"clientId\":\"${clientId.id}\"}""")
  }

  it should "deserialize an insert operation" in {
    val operationId = OperationId()
    val clientId = ClientId()
    val protocol = new JsonFormicJsonDataTypeProtocol(DataTypeName("json"))
    val json = s"""{\"accessPath\":[0,1],\"object\":{\"foo\":5.2},\"operationId\":\"${operationId.id}\",\"operationContext\":[],\"clientId\":\"${clientId.id}\"}"""

    val deserialized = protocol.deserializeOperation(json)

    deserialized should equal(TreeInsertOperation(AccessPath(0, 1), ObjectNode(null, List(NumberNode("foo", 5.2))), operationId, OperationContext(), clientId))
  }

  it should "deserialize a delete operation" in {
    val operationId = OperationId()
    val clientId = ClientId()
    val protocol = new JsonFormicJsonDataTypeProtocol(DataTypeName("json"))
    val json = s"""{\"accessPath\":[5,6],\"operationId\":\"${operationId.id}\",\"operationContext\":[],\"clientId\":\"${clientId.id}\"}"""

    val deserialized = protocol.deserializeOperation(json)

    deserialized should equal(TreeDeleteOperation(AccessPath(5, 6), operationId, OperationContext(), clientId))
  }

  it should "deserialize a no operation" in {
    val operationId = OperationId()
    val clientId = ClientId()
    val protocol = new JsonFormicJsonDataTypeProtocol(DataTypeName("json"))
    val json = s"""{\"accessPath\":[-1],\"operationId\":\"${operationId.id}\",\"operationContext\":[],\"clientId\":\"${clientId.id}\"}"""

    val deserialized = protocol.deserializeOperation(json)

    deserialized should equal(TreeNoOperation(operationId, OperationContext(), clientId))
  }

  it should "deserialize a replace operation" in {
    val operationId = OperationId()
    val clientId = ClientId()
    val protocol = new JsonFormicJsonDataTypeProtocol(DataTypeName("json"))
    val json = s"""{\"type\":\"replace\",\"accessPath\":[0,1],\"object\":{\"foo\":5.2},\"operationId\":\"${operationId.id}\",\"operationContext\":[],\"clientId\":\"${clientId.id}\"}"""

    val deserialized = protocol.deserializeOperation(json)

    deserialized should equal(JsonReplaceOperation(AccessPath(0, 1), NumberNode("foo", 5.2), operationId, OperationContext(), clientId))

  }
}
