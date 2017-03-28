package de.tu_berlin.formic.datatype.tree

import de.tu_berlin.formic.common.datatype.{DataStructureName, OperationContext}
import de.tu_berlin.formic.common.{ClientId, OperationId}
import org.scalatest.{FlatSpec, Matchers}

/**
  * @author Ronny Bräunlich
  */
class TreeFormicJsonDataTypeProtocolSpec extends FlatSpec with Matchers {

  "Tree Formic JSON data type protocol" should "serialize an insert operation" in {
    val operationId = OperationId()
    val clientId = ClientId()
    val protocol = new TreeFormicJsonDataTypeProtocol[Int](DataStructureName("intTree"))
    val operation = TreeInsertOperation(AccessPath(0, 1), ValueTreeNode(100, List(ValueTreeNode(1), ValueTreeNode(2))), operationId, OperationContext(), clientId)

    val serialized = protocol.serializeOperation(operation)

    serialized should equal(s"""{\"accessPath\":[0,1],\"tree\":{\"value\":100,\"children\":[{\"value\":1,\"children\":[]},{\"value\":2,\"children\":[]}]},\"operationId\":\"${operationId.id}\",\"operationContext\":[],\"clientId\":\"${clientId.id}\"}""")
  }

  it should "serialize a delete operation" in {
    val operationId = OperationId()
    val clientId = ClientId()
    val protocol = new TreeFormicJsonDataTypeProtocol[Int](DataStructureName("intTree"))
    val operation = TreeDeleteOperation(AccessPath(5, 6), operationId, OperationContext(), clientId)

    val serialized = protocol.serializeOperation(operation)

    serialized should equal(s"""{\"accessPath\":[5,6],\"operationId\":\"${operationId.id}\",\"operationContext\":[],\"clientId\":\"${clientId.id}\"}""")
  }

  it should "serialize a no operation" in {
    val operationId = OperationId()
    val clientId = ClientId()
    val protocol = new TreeFormicJsonDataTypeProtocol[Int](DataStructureName("intTree"))
    val operation = TreeNoOperation(operationId, OperationContext(), clientId)

    val serialized = protocol.serializeOperation(operation)

    serialized should equal(s"""{\"accessPath\":[-1],\"operationId\":\"${operationId.id}\",\"operationContext\":[],\"clientId\":\"${clientId.id}\"}""")

  }

  it should "deserialize an insert operation" in {
    val operationId = OperationId()
    val clientId = ClientId()
    val protocol = new TreeFormicJsonDataTypeProtocol[Int](DataStructureName("intTree"))
    val json = s"""{\"accessPath\":[0,1],\"tree\":{\"value\":100,\"children\":[{\"value\":1,\"children\":[]}]},\"operationId\":\"${operationId.id}\",\"operationContext\":[],\"clientId\":\"${clientId.id}\"}"""

    val deserialized = protocol.deserializeOperation(json)

    deserialized should equal(TreeInsertOperation(AccessPath(0, 1), ValueTreeNode(100, List(ValueTreeNode(1))), operationId, OperationContext(), clientId))
  }

  it should "deserialize a delete operation" in {
    val operationId = OperationId()
    val clientId = ClientId()
    val protocol = new TreeFormicJsonDataTypeProtocol[Int](DataStructureName("intTree"))
    val json = s"""{\"accessPath\":[5,6],\"operationId\":\"${operationId.id}\",\"operationContext\":[],\"clientId\":\"${clientId.id}\"}"""

    val deserialized = protocol.deserializeOperation(json)

    deserialized should equal(TreeDeleteOperation(AccessPath(5, 6), operationId, OperationContext(), clientId))
  }

  it should "deserialize a no operation" in {
    val operationId = OperationId()
    val clientId = ClientId()
    val protocol = new TreeFormicJsonDataTypeProtocol[Int](DataStructureName("intTree"))
    val json = s"""{\"accessPath\":[-1],\"operationId\":\"${operationId.id}\",\"operationContext\":[],\"clientId\":\"${clientId.id}\"}"""

    val deserialized = protocol.deserializeOperation(json)

    deserialized should equal(TreeNoOperation(operationId, OperationContext(), clientId))
  }
}
