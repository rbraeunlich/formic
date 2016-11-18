package de.tu_berlin.formic.server.datatype

import de.tu_berlin.formic.common.datatype._
import de.tu_berlin.formic.common.json.FormicJsonDataTypeProtocol
import de.tu_berlin.formic.common.{ClientId, DataTypeInstanceId, OperationId}
import org.scalatest.Assertions._
import upickle.Js

/**
  * @author Ronny Bräunlich
  */

class TestDataTypeFactory extends AbstractDataTypeFactory[TestDataType] {
  override def create(dataTypeInstanceId: DataTypeInstanceId): TestDataType = new TestDataType(new HistoryBuffer, dataTypeInstanceId)

  override val name: DataTypeName = TestClasses.dataTypeName
}

class TestDataType(override val historyBuffer: HistoryBuffer, val dataTypeInstanceId: DataTypeInstanceId) extends AbstractDataType(dataTypeInstanceId) {

  var data = "{data}"

  override def apply(op: DataTypeOperation): Unit = {
    op match {
      case test: TestOperation => data = "{received}"
      case _ => fail
    }
  }

  override val dataTypeName: DataTypeName = TestClasses.dataTypeName

  override def getDataAsJson: String = data
}

case class TestOperation(id: OperationId, operationContext: OperationContext, clientId: ClientId) extends DataTypeOperation

class TestFormicJsonDataTypeProtocol extends FormicJsonDataTypeProtocol {

  override def deserializeOperation(json: String): DataTypeOperation = {
    val valueMap = upickle.json.read(json).obj
    TestOperation(
      OperationId(valueMap("operationId").str),
      OperationContext(valueMap("operationContext").arr.map(v => OperationId(v.str)).toList),
      ClientId(valueMap("clientId").str))
  }

  override val name: DataTypeName = TestClasses.dataTypeName

  override def serializeOperation(op: DataTypeOperation): String = {
    Js.Obj(
      ("operationId", Js.Str(op.id.id)),
      ("operationContext", Js.Arr(op.operationContext.operations.map(o => Js.Str(o.id)): _*)),
      ("clientId", Js.Str(op.clientId.id))
    ).toString()
  }
}

object TestClasses {
  val dataTypeName = DataTypeName("Test")
}
