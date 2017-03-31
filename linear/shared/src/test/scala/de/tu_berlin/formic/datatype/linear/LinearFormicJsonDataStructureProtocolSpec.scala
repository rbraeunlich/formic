package de.tu_berlin.formic.datatype.linear

import de.tu_berlin.formic.common.datastructure.{DataStructureName, OperationContext}
import de.tu_berlin.formic.common.{ClientId, OperationId}
import org.scalatest._
import upickle.default._

/**
  * @author Ronny Bräunlich
  */
class LinearFormicJsonDataStructureProtocolSpec extends FlatSpec with Matchers {

  "Linear JSON protocol" should "serialize a linear insert operation" in {
    val protocol = new LinearFormicJsonDataStructureProtocol[Char](DataStructureName("string"))
    val index = 0
    val value = Integer.valueOf(1)
    val operationId = OperationId()
    val context = OperationContext(List.empty)
    val clientId = ClientId()
    val op = LinearInsertOperation(index, value, operationId, context, clientId)

    val serialized = protocol.serializeOperation(op)

    serialized should equal(s"""{\"index\":0,\"object\":\"1\",\"operationId\":\"${operationId.id}\",\"operationContext\":[],\"clientId\":\"${clientId.id}\"}""")
  }

  it should "serialize a linear delete operation" in {
    val protocol = new LinearFormicJsonDataStructureProtocol[Char](DataStructureName("string"))
    val index = 0
    val operationId = OperationId()
    val context = OperationContext(List.empty)
    val clientId = ClientId()
    val op = LinearDeleteOperation(index, operationId, context, clientId)

    val serialized = protocol.serializeOperation(op)

    serialized should equal(s"""{\"index\":0,\"operationId\":\"${operationId.id}\",\"operationContext\":[],\"clientId\":\"${clientId.id}\"}""")
  }

  it should "serialize a no operation" in {
    val protocol = new LinearFormicJsonDataStructureProtocol[Char](DataStructureName("string"))
    val operationId = OperationId()
    val context = OperationContext(List.empty)
    val clientId = ClientId()
    val op = LinearNoOperation(id = operationId, operationContext = context, clientId = clientId)

    val serialized = protocol.serializeOperation(op)

    serialized should equal(s"""{\"index\":-1,\"operationId\":\"${operationId.id}\",\"operationContext\":[],\"clientId\":\"${clientId.id}\"}""")
  }

  it should "deserialize a linear insert operation" in {
    val protocol = new LinearFormicJsonDataStructureProtocol[Char](DataStructureName("string"))
    val json = "{\"index\":0,\"object\":\"a\",\"operationId\":\"1\",\"operationContext\":[],\"clientId\":\"123\"}"

    val op = protocol.deserializeOperation(json)

    op should be(LinearInsertOperation(0, 'a', OperationId("1"), OperationContext(List.empty), ClientId("123")))
  }

  it should "deserialize a linear delete operation" in {
    val protocol = new LinearFormicJsonDataStructureProtocol[Char](DataStructureName("string"))
    val json = "{\"index\":0,\"operationId\":\"1\",\"operationContext\":[],\"clientId\":\"123\"}"

    val op = protocol.deserializeOperation(json)

    op should be(LinearDeleteOperation(0, OperationId("1"), OperationContext(List.empty), ClientId("123")))
  }

  it should "deserialize a no operation" in {
    val protocol = new LinearFormicJsonDataStructureProtocol[Char](DataStructureName("string"))
    val json = s"""{\"index\":-1,\"operationId\":\"567\",\"operationContext\":[],\"clientId\":\"89\"}"""

    val op = protocol.deserializeOperation(json)

    op should equal(LinearNoOperation(OperationId("567"), OperationContext(), ClientId("89")))
  }
}
