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
    val operation = TreeInsertOperation(AccessPath(0, 1), TreeNode(100, ArrayBuffer(TreeNode(1), TreeNode(2))), operationId, OperationContext(), clientId)

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
}
