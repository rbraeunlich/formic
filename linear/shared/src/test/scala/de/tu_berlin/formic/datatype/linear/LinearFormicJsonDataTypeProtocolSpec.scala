package de.tu_berlin.formic.datatype.linear

import de.tu_berlin.formic.common.datatype.{DataTypeName, OperationContext}
import de.tu_berlin.formic.common.{ClientId, OperationId}
import org.scalatest._
import upickle.default._

import scala.runtime.RichChar
/**
  * @author Ronny Bräunlich
  */
class LinearFormicJsonDataTypeProtocolSpec extends FlatSpec with Matchers {

  "Linear JSON protocol" should "serialize a linear insert operation" in {
    val protocol = new LinearFormicJsonDataTypeProtocol[Char](DataTypeName("string"))
    val index = 0
    val value = Integer.valueOf(1)
    val operationId = OperationId()
    val context = OperationContext(List.empty)
    val clientId = ClientId()
    val op = LinearInsertOperation(index, value, operationId, context, clientId)

    val serialized = protocol.serializeOperation(op)

    serialized should equal(s"""{\n    \"index\": 0,\n    \"object\": \"1\",\n    \"operationId\": \"${operationId.id}\",\n    \"operationContext\": [],\n    \"clientId\": \"${clientId.id}\"\n}""")
  }

  it should "serialize a linear delete operation" in {
    val protocol = new LinearFormicJsonDataTypeProtocol[Char](DataTypeName("string"))
    val index = 0
    val operationId = OperationId()
    val context = OperationContext(List.empty)
    val clientId = ClientId()
    val op = LinearDeleteOperation(index, operationId, context, clientId)

    val serialized = protocol.serializeOperation(op)

    serialized should equal(s"""{\n    \"index\": 0,\n    \"operationId\": \"${operationId.id}\",\n    \"operationContext\": [],\n    \"clientId\": \"${clientId.id}\"\n}""")
  }

  it should "deserialize a linear insert operation" in {
    val protocol = new LinearFormicJsonDataTypeProtocol[Char](DataTypeName("string"))
    val json = "{\"index\":0,\"object\":\"a\",\"operationId\":\"1\",\"operationContext\":[],\"clientId\":\"123\"}"

    val op = protocol.deserializeOperation(json)

    op should be(LinearInsertOperation(0, 'a', OperationId("1"), OperationContext(List.empty), ClientId("123")))
  }

  it should "deserialize a linear delete operation" in {
    val protocol = new LinearFormicJsonDataTypeProtocol[Char](DataTypeName("string"))
    val json = "{\"index\":0,\"operationId\":\"1\",\"operationContext\":[],\"clientId\":\"123\"}"

    val op = protocol.deserializeOperation(json)

    op should be(LinearDeleteOperation(0, OperationId("1"), OperationContext(List.empty), ClientId("123")))

  }
}
