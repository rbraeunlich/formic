package de.tu_berlin.formic.client

import akka.actor.ActorRef
import de.tu_berlin.formic.common.controlalgo.ControlAlgorithmClient
import de.tu_berlin.formic.common.datastructure._
import de.tu_berlin.formic.common.datastructure.client.{AbstractClientDataStructure, AbstractClientDataStructureFactory, DataStructureInitiator}
import de.tu_berlin.formic.common.json.FormicJsonDataStructureProtocol
import de.tu_berlin.formic.common.{ClientId, DataStructureInstanceId, OperationId}
import org.scalatest.Assertions._
import upickle.Js

/**
  * @author Ronny Bräunlich
  */

class TestDataStructureFactory extends AbstractClientDataStructureFactory[TestClientDataStructure, TestFormicDataStructure] {

  override val name: DataStructureName = TestClasses.dataStructureName

  override def createDataStructure(dataTypeInstanceId: DataStructureInstanceId, outgoingConnection: ActorRef, data: Option[String], lastOperationId: Option[OperationId] = Option.empty): TestClientDataStructure = new TestClientDataStructure(new HistoryBuffer, dataTypeInstanceId, TestControlAlgorithm, data, lastOperationId, outgoingConnection)

  override def createWrapper(dataTypeInstanceId: DataStructureInstanceId, dataType: ActorRef, clientId: ClientId): TestFormicDataStructure = new TestFormicDataStructure
}

class TestClientDataStructure(override val historyBuffer: HistoryBuffer, val dataTypeInstanceId: DataStructureInstanceId, controlAlgorithm: ControlAlgorithmClient, initialData: Option[String] = Option.empty, lastOperationId: Option[OperationId], outgoingConnection: ActorRef) extends AbstractClientDataStructure(dataTypeInstanceId, controlAlgorithm, lastOperationId, outgoingConnection) {

  var data = initialData.getOrElse("{data}")

  override def apply(op: DataStructureOperation): Unit = {
    op match {
      case test: TestOperation => data = "{received}"
      case _ => fail
    }
  }

  override val dataStructureName: DataStructureName = TestClasses.dataStructureName

  override def getDataAsJson: String = data

  override val transformer: OperationTransformer = TestTransformer

  override def cloneOperationWithNewContext(op: DataStructureOperation, context: OperationContext): DataStructureOperation = op
}

class TestFormicDataStructure(actor: ActorRef = null) extends FormicDataStructure((_) => {}, TestClasses.dataStructureName, actor, ClientId(), DataStructureInstanceId(), new DataStructureInitiator {
  override def initDataStructure(dataType: FormicDataStructure): Unit = {}
}) {
}

case class TestOperation(id: OperationId, operationContext: OperationContext, var clientId: ClientId) extends DataStructureOperation

class TestFormicJsonDataStructureProtocol extends FormicJsonDataStructureProtocol {

  override def deserializeOperation(json: String): DataStructureOperation = {
    val valueMap = upickle.json.read(json).obj
    TestOperation(
      OperationId(valueMap("operationId").str),
      OperationContext(valueMap("operationContext").arr.map(v => OperationId(v.str)).toList),
      ClientId(valueMap("clientId").str))
  }

  override val name: DataStructureName = TestClasses.dataStructureName

  override def serializeOperation(op: DataStructureOperation): String = {
    Js.Obj(
      ("operationId", Js.Str(op.id.id)),
      ("operationContext", Js.Arr(op.operationContext.operations.map(o => Js.Str(o.id)): _*)),
      ("clientId", Js.Str(op.clientId.id))
    ).toString()
  }
}

object TestControlAlgorithm extends ControlAlgorithmClient {

  override def canBeApplied(op: DataStructureOperation, history: HistoryBuffer): Boolean = true

  override def transform(op: DataStructureOperation, history: HistoryBuffer, transformer: OperationTransformer): DataStructureOperation = op

  override def canLocalOperationBeApplied(op: DataStructureOperation): Boolean = true

  override def currentOperationContext: OperationContext = OperationContext(List.empty)
}

object TestTransformer extends OperationTransformer {

  override def transform(pair: (DataStructureOperation, DataStructureOperation)): DataStructureOperation = pair._1

  override def bulkTransform(operation: DataStructureOperation, bridge: List[DataStructureOperation]): List[DataStructureOperation] = bridge

  override protected def transformInternal(pair: (DataStructureOperation, DataStructureOperation), withNewContext: Boolean): DataStructureOperation = pair._2
}

object TestClasses {
  val dataStructureName = DataStructureName("Test")
}
