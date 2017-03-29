package de.tu_berlin.formic.server.datatype

import akka.actor.{ActorRef, ActorSystem, Props}
import de.tu_berlin.formic.common.controlalgo.ControlAlgorithm
import de.tu_berlin.formic.common.datatype._
import de.tu_berlin.formic.common.json.{FormicJsonDataTypeProtocol, FormicJsonProtocol}
import de.tu_berlin.formic.common.server.datatype.{AbstractServerDataStructure$, AbstractServerDataTypeFactory}
import de.tu_berlin.formic.common.{ClientId, DataStructureInstanceId, OperationId}
import org.scalatest.Assertions._
import upickle.Js

/**
  * @author Ronny Bräunlich
  */

class TestDataTypeFactory extends AbstractServerDataTypeFactory[TestServerDataStructure] {

  override def create(dataTypeInstanceId: DataStructureInstanceId): TestServerDataStructure = new TestServerDataStructure(new HistoryBuffer, dataTypeInstanceId, TestControlAlgorithm)

  override val name: DataStructureName = TestClasses.dataTypeName
}

class TestServerDataStructure(override val historyBuffer: HistoryBuffer, val dataTypeInstanceId: DataStructureInstanceId, controlAlgorithm: ControlAlgorithm) extends AbstractServerDataStructure(dataTypeInstanceId, controlAlgorithm) {



  var data = "{data}"

  override def apply(op: DataTypeOperation): Unit = {
    op match {
      case test: TestOperation => data = "{received}"
      case _ => fail
    }
  }

  override val dataTypeName: DataStructureName = TestClasses.dataTypeName

  override def getDataAsJson: String = data

  override val transformer: OperationTransformer = TestTransformer
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

  override val name: DataStructureName = TestClasses.dataTypeName

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

  override def bulkTransform(operation: DataTypeOperation, bridge: List[DataTypeOperation]): List[DataTypeOperation] = bridge

  override protected def transformInternal(pair: (DataTypeOperation, DataTypeOperation), withNewContext: Boolean): DataTypeOperation = pair._1
}

object TestClasses {
  val dataTypeName = DataStructureName("Test")
}

object TestClassProvider extends ServerDataStructureProvider {
  override def initFactories(actorSystem: ActorSystem): Map[DataStructureName, ActorRef] = {
    val actor = actorSystem.actorOf(Props(new TestDataTypeFactory), TestClasses.dataTypeName.name)
    Map(TestClasses.dataTypeName -> actor)
  }

  override def registerFormicJsonDataStructureProtocols(formicJsonProtocol: FormicJsonProtocol): Unit = {
    formicJsonProtocol.registerProtocol(new TestFormicJsonDataTypeProtocol)
  }
}
