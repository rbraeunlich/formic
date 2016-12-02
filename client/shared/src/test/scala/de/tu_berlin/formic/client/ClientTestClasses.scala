package de.tu_berlin.formic.client

import akka.actor.ActorRef
import de.tu_berlin.formic.client.datatype.AbstractClientDataTypeFactory
import de.tu_berlin.formic.common.controlalgo.ControlAlgorithm
import de.tu_berlin.formic.common.datatype._
import de.tu_berlin.formic.common.json.FormicJsonDataTypeProtocol
import de.tu_berlin.formic.common.{ClientId, DataTypeInstanceId, OperationId}
import org.scalatest.Assertions._
import upickle.Js

/**
  * @author Ronny Bräunlich
  */

class TestDataTypeFactory extends AbstractClientDataTypeFactory[TestDataType, TestFormicDataType](null) {

  override val name: DataTypeName = TestClasses.dataTypeName

  override def createDataType(dataTypeInstanceId: DataTypeInstanceId, outgoingConnection: ActorRef): TestDataType = new TestDataType(new HistoryBuffer, dataTypeInstanceId, TestControlAlgorithm)

  override def createWrapperType(dataTypeInstanceId: DataTypeInstanceId, dataType: ActorRef): TestFormicDataType = new TestFormicDataType
}

class TestDataType(override val historyBuffer: HistoryBuffer, val dataTypeInstanceId: DataTypeInstanceId, controlAlgorithm: ControlAlgorithm) extends AbstractDataType(dataTypeInstanceId, controlAlgorithm) {

  var data = "{data}"

  override def apply(op: DataTypeOperation): Unit = {
    op match {
      case test: TestOperation => data = "{received}"
      case _ => fail
    }
  }

  override val dataTypeName: DataTypeName = TestClasses.dataTypeName

  override def getDataAsJson: String = data

  override val transformer: OperationTransformer = TestTransformer
}

class TestFormicDataType extends FormicDataType {
  override var callback: () => Unit = () => {}
  override val dataTypeName: DataTypeName = TestClasses.dataTypeName
}

case class TestOperation(id: OperationId, operationContext: OperationContext,var clientId: ClientId) extends DataTypeOperation

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

object TestControlAlgorithm extends ControlAlgorithm {

  override def canBeApplied(op: DataTypeOperation, history: HistoryBuffer): Boolean = true

  override def transform(op: DataTypeOperation, history: HistoryBuffer, transformer: OperationTransformer): DataTypeOperation = op
}

object TestTransformer extends OperationTransformer {

  override def transform(pair: (DataTypeOperation, DataTypeOperation)): DataTypeOperation = pair._1
}

object TestClasses {
  val dataTypeName = DataTypeName("Test")
}
