package de.tu_berlin.formic.common.datatype.persistence

import akka.actor.{ActorSystem, Props}
import akka.testkit.TestProbe
import de.tu_berlin.formic.common.controlalgo.ControlAlgorithm
import de.tu_berlin.formic.common.datatype._
import de.tu_berlin.formic.common.datatype.persistence.AbstractServerDataTypePersistenceSpec._
import de.tu_berlin.formic.common.message.{OperationMessage, UpdateRequest, UpdateResponse}
import de.tu_berlin.formic.common.server.datatype.AbstractServerDataStructure
import de.tu_berlin.formic.common.{ClientId, DataStructureInstanceId, OperationId}
import org.scalatest.Assertions._

/**
  * @author Ronny Bräunlich
  */
class AbstractServerDataTypePersistenceSpec extends PersistenceSpec(ActorSystem("AbstractServerDataTypePersistenceSpec"))
  with PersistenceCleanup {

  "An AbstractServerDataType" should {
    "re-apply stored operations after recovery" in {
      val probe = TestProbe()
      system.eventStream.subscribe(probe.ref, classOf[OperationMessage])
      val id = DataStructureInstanceId()
      val dataType = system.actorOf(Props(new AbstractServerDataTypePersistenceSpec.AbstractServerDataTypePersistenceSpecServerDataStructure(id, new AbstractServerDataTypePersistenceSpecControlAlgorithm)), id.id)
      val op1 = AbstractServerDataStructurePersistenceSpecOperation(OperationId(), OperationContext(), ClientId())
      val op2 = AbstractServerDataStructurePersistenceSpecOperation(OperationId(), OperationContext(List(op1.id)), ClientId())
      val msg1 = OperationMessage(ClientId(), id, dataTypeName ,List(op1))
      val msg2 = OperationMessage(ClientId(), id, dataTypeName ,List(op2))

      dataType ! msg1
      dataType ! msg2
      probe.receiveN(2)

      killActors(dataType)

      val recoveredActor = system.actorOf(Props(new AbstractServerDataTypePersistenceSpec.AbstractServerDataTypePersistenceSpecServerDataStructure(id, new AbstractServerDataTypePersistenceSpecControlAlgorithm)), id.id)

      recoveredActor ! UpdateRequest(ClientId(), id)

      expectMsg(UpdateResponse(id, dataTypeName, "{received}", Some(op2.id)))
    }
  }
}

object AbstractServerDataTypePersistenceSpec {

  val dataTypeName = DataStructureName("persistence")

  case class AbstractServerDataStructurePersistenceSpecOperation(id: OperationId, operationContext: OperationContext, var clientId: ClientId) extends DataStructureOperation

  class AbstractServerDataTypePersistenceSpecControlAlgorithm(var canBeApplied: Boolean = true) extends ControlAlgorithm {

    override def canBeApplied(op: DataStructureOperation, history: HistoryBuffer): Boolean = canBeApplied

    override def transform(op: DataStructureOperation, history: HistoryBuffer, transformer: OperationTransformer): DataStructureOperation = op
  }

  class AbstractServerDataTypePersistenceSpecServerDataStructure(id: DataStructureInstanceId, controlAlgorithm: ControlAlgorithm) extends AbstractServerDataStructure(id, controlAlgorithm) {

    val transformer = new OperationTransformer {
      override def transform(pair: (DataStructureOperation, DataStructureOperation)): DataStructureOperation = pair._1

      override def bulkTransform(operation: DataStructureOperation, bridge: List[DataStructureOperation]): List[DataStructureOperation] = bridge

      override protected def transformInternal(pair: (DataStructureOperation, DataStructureOperation), withNewContext: Boolean): DataStructureOperation = pair._1
    }

    var data = "{data}"

    override def apply(op: DataStructureOperation): Unit = {
      op match {
        case test: AbstractServerDataStructurePersistenceSpecOperation => data = "{received}"
        case _ => fail
      }
    }

    override val dataTypeName: DataStructureName = AbstractServerDataTypePersistenceSpec.dataTypeName

    override def getDataAsJson: String = data
  }

}