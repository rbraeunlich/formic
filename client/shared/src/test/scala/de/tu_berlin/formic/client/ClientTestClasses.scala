package de.tu_berlin.formic.client

import akka.actor.ActorRef
import de.tu_berlin.formic.common.controlalgo.ControlAlgorithmClient
import de.tu_berlin.formic.common.datatype._
import de.tu_berlin.formic.common.datatype.client.{AbstractClientDataStructure, AbstractClientDataTypeFactory, DataStructureInitiator}
import de.tu_berlin.formic.common.json.FormicJsonDataTypeProtocol
import de.tu_berlin.formic.common.{ClientId, DataStructureInstanceId, OperationId}
import org.scalatest.Assertions._
import upickle.Js

/**
  * @author Ronny Bräunlich
  */

class TestDataTypeFactory extends AbstractClientDataTypeFactory[TestClientDataStructure, TestFormicDataStructure] {

  override val name: DataStructureName = TestClasses.dataTypeName

  override def createDataType(dataTypeInstanceId: DataStructureInstanceId, outgoingConnection: ActorRef, data: Option[String], lastOperationId: Option[OperationId] = Option.empty): TestClientDataStructure = new TestClientDataStructure(new HistoryBuffer, dataTypeInstanceId, TestControlAlgorithm, data, lastOperationId, outgoingConnection)

  override def createWrapperType(dataTypeInstanceId: DataStructureInstanceId, dataType: ActorRef, clientId: ClientId): TestFormicDataStructure = new TestFormicDataStructure
}

class TestClientDataStructure(override val historyBuffer: HistoryBuffer, val dataTypeInstanceId: DataStructureInstanceId, controlAlgorithm: ControlAlgorithmClient, initialData: Option[String] = Option.empty, lastOperationId: Option[OperationId], outgoingConnection: ActorRef) extends AbstractClientDataStructure(dataTypeInstanceId, controlAlgorithm, lastOperationId, outgoingConnection) {

  var data = initialData.getOrElse("{data}")

  override def apply(op: DataTypeOperation): Unit = {
    op match {
      case test: TestOperation => data = "{received}"
      case _ => fail
    }
  }

  override val dataTypeName: DataStructureName = TestClasses.dataTypeName

  override def getDataAsJson: String = data

  override val transformer: OperationTransformer = TestTransformer

  override def cloneOperationWithNewContext(op: DataTypeOperation, context: OperationContext): DataTypeOperation = op
}

class TestFormicDataStructure(actor: ActorRef = null) extends FormicDataStructure((_) => {}, TestClasses.dataTypeName, actor, ClientId(), DataStructureInstanceId(), new DataStructureInitiator {
  override def initDataStructure(dataType: FormicDataStructure): Unit = {}
}) {
}

case class TestOperation(id: OperationId, operationContext: OperationContext, var clientId: ClientId) extends DataTypeOperation

class TestFormicJsonDataTypeProtocol extends FormicJsonDataTypeProtocol {

  override def deserializeOperation(json: String): DataTypeOperation = {
    val valueMap = upickle.json.read(json).obj
    TestOperation(
      OperationId(valueMap("operationId").str),
      OperationContext(valueMap("operationContext").arr.map(v => OperationId(v.str)).toList),
      ClientId(valueMap("clientId").str))
  }

  override val name: DataStructureName = TestClasses.dataTypeName

  override def serializeOperation(op: DataTypeOperation): String = {
    Js.Obj(
      ("operationId", Js.Str(op.id.id)),
      ("operationContext", Js.Arr(op.operationContext.operations.map(o => Js.Str(o.id)): _*)),
      ("clientId", Js.Str(op.clientId.id))
    ).toString()
  }
}

object TestControlAlgorithm extends ControlAlgorithmClient {

  override def canBeApplied(op: DataTypeOperation, history: HistoryBuffer): Boolean = true

  override def transform(op: DataTypeOperation, history: HistoryBuffer, transformer: OperationTransformer): DataTypeOperation = op

  override def canLocalOperationBeApplied(op: DataTypeOperation): Boolean = true

  override def currentOperationContext: OperationContext = OperationContext(List.empty)
}

object TestTransformer extends OperationTransformer {

  override def transform(pair: (DataTypeOperation, DataTypeOperation)): DataTypeOperation = pair._1

  override def bulkTransform(operation: DataTypeOperation, bridge: List[DataTypeOperation]): List[DataTypeOperation] = bridge

  override protected def transformInternal(pair: (DataTypeOperation, DataTypeOperation), withNewContext: Boolean): DataTypeOperation = pair._2
}

object TestClasses {
  val dataTypeName = DataStructureName("Test")
}
