package de.tu_berlin.formic.server.datatype

import akka.actor.{ActorRef, ActorSystem, Props}
import de.tu_berlin.formic.common.controlalgo.ControlAlgorithm
import de.tu_berlin.formic.common.datastructure._
import de.tu_berlin.formic.common.json.{FormicJsonDataStructureProtocol, FormicJsonProtocol}
import de.tu_berlin.formic.common.server.datastructure.{AbstractServerDataStructure, AbstractServerDataStructureFactory}
import de.tu_berlin.formic.common.{ClientId, DataStructureInstanceId, OperationId}
import org.scalatest.Assertions._
import upickle.Js

/**
  * @author Ronny Bräunlich
  */

class TestDataStructureFactory extends AbstractServerDataStructureFactory[TestServerDataStructure] {

  override def create(dataTypeInstanceId: DataStructureInstanceId): TestServerDataStructure = new TestServerDataStructure(new HistoryBuffer, dataTypeInstanceId, TestControlAlgorithm)

  override val name: DataStructureName = TestClasses.dataTypeName
}

class TestServerDataStructure(override val historyBuffer: HistoryBuffer, val dataTypeInstanceId: DataStructureInstanceId, controlAlgorithm: ControlAlgorithm) extends AbstractServerDataStructure(dataTypeInstanceId, controlAlgorithm) {



  var data = "{data}"

  override def apply(op: DataStructureOperation): Unit = {
    op match {
      case test: TestOperation => data = "{received}"
      case _ => fail
    }
  }

  override val dataTypeName: DataStructureName = TestClasses.dataTypeName

  override def getDataAsJson: String = data

  override val transformer: OperationTransformer = TestTransformer
}

case class TestOperation(id: OperationId, operationContext: OperationContext,var clientId: ClientId) extends DataStructureOperation

class TestFormicJsonDataStructureProtocol extends FormicJsonDataStructureProtocol {

  override def deserializeOperation(json: String): DataStructureOperation = {
    val valueMap = upickle.json.read(json).obj
    TestOperation(
      OperationId(valueMap("operationId").str),
      OperationContext(valueMap("operationContext").arr.map(v => OperationId(v.str)).toList),
      ClientId(valueMap("clientId").str))
  }

  override val name: DataStructureName = TestClasses.dataTypeName

  override def serializeOperation(op: DataStructureOperation): String = {
    Js.Obj(
      ("operationId", Js.Str(op.id.id)),
      ("operationContext", Js.Arr(op.operationContext.operations.map(o => Js.Str(o.id)): _*)),
      ("clientId", Js.Str(op.clientId.id))
    ).toString()
  }
}

object TestControlAlgorithm extends ControlAlgorithm {

  override def canBeApplied(op: DataStructureOperation, history: HistoryBuffer): Boolean = true

  override def transform(op: DataStructureOperation, history: HistoryBuffer, transformer: OperationTransformer): DataStructureOperation = op
}

object TestTransformer extends OperationTransformer {

  override def transform(pair: (DataStructureOperation, DataStructureOperation)): DataStructureOperation = pair._1

  override def bulkTransform(operation: DataStructureOperation, bridge: List[DataStructureOperation]): List[DataStructureOperation] = bridge

  override protected def transformInternal(pair: (DataStructureOperation, DataStructureOperation), withNewContext: Boolean): DataStructureOperation = pair._1
}

object TestClasses {
  val dataTypeName = DataStructureName("Test")
}

object TestClassProvider extends ServerDataStructureProvider {
  override def initFactories(actorSystem: ActorSystem): Map[DataStructureName, ActorRef] = {
    val actor = actorSystem.actorOf(Props(new TestDataStructureFactory), TestClasses.dataTypeName.name)
    Map(TestClasses.dataTypeName -> actor)
  }

  override def registerFormicJsonDataStructureProtocols(formicJsonProtocol: FormicJsonProtocol): Unit = {
    formicJsonProtocol.registerProtocol(new TestFormicJsonDataStructureProtocol)
  }
}
