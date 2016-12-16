package de.tu_berlin.formic.datatype.tree

import de.tu_berlin.formic.common.datatype.{DataTypeName, OperationContext}
import de.tu_berlin.formic.common.{ClientId, OperationId}
import org.scalatest.{FlatSpec, Matchers}

import scala.collection.mutable.ArrayBuffer

/**
  * @author Ronny Bräunlich
  */
class TreeFormicJsonDataTypeProtocolSpec extends FlatSpec with Matchers {

  "Tree Formic JSON data type protocol" should "serialize an insert operation" in {
    val operationId = OperationId()
    val clientId = ClientId()
    val protocol = new TreeFormicJsonDataTypeProtocol[Int](DataTypeName("intTree"))
    val operation = TreeInsertOperation(AccessPath(0, 1), ValueTreeNode(100, List(ValueTreeNode(1), ValueTreeNode(2))), operationId, OperationContext(), clientId)

    val serialized = protocol.serializeOperation(operation)

    serialized should equal(s"""{\"accessPath\":[0,1],\"tree\":{\"value\":100,\"children\":[{\"value\":1,\"children\":[]},{\"value\":2,\"children\":[]}]},\"operationId\":\"${operationId.id}\",\"operationContext\":[],\"clientId\":\"${clientId.id}\"}""")
  }

  it should "serialize a delete operation" in {
    val operationId = OperationId()
    val clientId = ClientId()
    val protocol = new TreeFormicJsonDataTypeProtocol[Int](DataTypeName("intTree"))
    val operation = TreeDeleteOperation(AccessPath(5, 6), operationId, OperationContext(), clientId)

    val serialized = protocol.serializeOperation(operation)

    serialized should equal(s"""{\"accessPath\":[5,6],\"operationId\":\"${operationId.id}\",\"operationContext\":[],\"clientId\":\"${clientId.id}\"}""")
  }

  it should "deserialize an insert operation" in {
    val operationId = OperationId()
    val clientId = ClientId()
    val protocol = new TreeFormicJsonDataTypeProtocol[Int](DataTypeName("intTree"))
    val json = s"""{\"accessPath\":[0,1],\"tree\":{\"value\":100,\"children\":[{\"value\":1,\"children\":[]}]},\"operationId\":\"${operationId.id}\",\"operationContext\":[],\"clientId\":\"${clientId.id}\"}"""

    val deserialized = protocol.deserializeOperation(json)

    deserialized shouldBe a[TreeInsertOperation]
    deserialized.asInstanceOf[TreeInsertOperation].clientId should equal(clientId)
    deserialized.asInstanceOf[TreeInsertOperation].id should equal(operationId)
    deserialized.asInstanceOf[TreeInsertOperation].operationContext should equal(OperationContext())
    deserialized.asInstanceOf[TreeInsertOperation].tree should equal(ValueTreeNode(100, List(ValueTreeNode(1))))
    deserialized.asInstanceOf[TreeInsertOperation].accessPath should equal(AccessPath(0, 1))
  }

  it should "deserialize a delete operation" in {
    val operationId = OperationId()
    val clientId = ClientId()
    val protocol = new TreeFormicJsonDataTypeProtocol[Int](DataTypeName("intTree"))
    val json = s"""{\"accessPath\":[5,6],\"operationId\":\"${operationId.id}\",\"operationContext\":[],\"clientId\":\"${clientId.id}\"}"""

    val deserialized = protocol.deserializeOperation(json)

    deserialized shouldBe a[TreeDeleteOperation]
    deserialized.asInstanceOf[TreeDeleteOperation].clientId should equal(clientId)
    deserialized.asInstanceOf[TreeDeleteOperation].id should equal(operationId)
    deserialized.asInstanceOf[TreeDeleteOperation].operationContext should equal(OperationContext())
    deserialized.asInstanceOf[TreeDeleteOperation].accessPath should equal(AccessPath(5, 6))
  }
}
