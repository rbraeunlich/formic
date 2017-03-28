package de.tu_berlin.formic.datatype.tree.client

import akka.pattern._
import akka.util.Timeout
import de.tu_berlin.formic.common.datatype.FormicDataType.LocalOperationMessage
import de.tu_berlin.formic.common.datatype.client.{ClientDataTypeEvent, DataTypeInitiator}
import de.tu_berlin.formic.common.datatype.{DataTypeName, FormicDataType, OperationContext}
import de.tu_berlin.formic.common.message.{OperationMessage, UpdateRequest, UpdateResponse}
import de.tu_berlin.formic.common.{DataStructureInstanceId$, OperationId}
import de.tu_berlin.formic.datatype.tree._
import upickle.default._

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js.annotation.JSExport

/**
  * @author Ronny Bräunlich
  */
class FormicTree[T](_callback: (ClientDataTypeEvent) => Unit,
                    initiator: DataTypeInitiator,
                    dataTypeInstanceId: DataStructureInstanceId,
                    dataTypeName: DataTypeName)
                   (implicit val writer: Writer[T], val reader: Reader[T])
  extends FormicDataType(_callback, dataTypeName, dataTypeInstanceId = dataTypeInstanceId, initiator = initiator) {

  //TODO I suppose getDataAsJson within the TreeClientDataType is quite slow, optimize
  implicit val timeout: Timeout = 3.seconds

  implicit val valueTreeNodeReader = new ValueTreeNodeReader[T]()

  @JSExport
  def insert(value: T, path: AccessPath): OperationId = {
    val operationId = OperationId()
    actor ! LocalOperationMessage(
      OperationMessage(clientId, dataTypeInstanceId, dataTypeName, List(
        TreeInsertOperation(path, ValueTreeNode(value), operationId, OperationContext(), clientId)
      ))
    )
    operationId
  }

  @JSExport
  def remove(path: AccessPath): OperationId = {
    val operationId = OperationId()
    actor ! LocalOperationMessage(
      OperationMessage(clientId, dataTypeInstanceId, dataTypeName, List(
        TreeDeleteOperation(path, operationId, OperationContext(), clientId)
      ))
    )
    operationId
  }

  @JSExport
  def getSubTree(path: AccessPath)(implicit ec: ExecutionContext): Future[TreeNode] = {
    ask(actor, UpdateRequest(clientId, dataTypeInstanceId)).
      mapTo[UpdateResponse].
      map(rep => {
        rep.data
      }).
      map(data => {
        if(data == "") EmptyTreeNode
        else read[ValueTreeNode](data)
      }).
      map(tree => tree.getNode(path))
  }

  @JSExport
  def getTree()(implicit ec: ExecutionContext): Future[TreeNode] = {
    ask(actor, UpdateRequest(clientId, dataTypeInstanceId)).
      mapTo[UpdateResponse].
      map(rep => {
        rep.data
      }).
      map(data => {
        if(data == "") EmptyTreeNode
        else read[ValueTreeNode](data)
      })
  }
}
