package de.tu_berlin.formic.common.datatype.persistence

import akka.actor.{ActorSystem, Props}
import akka.testkit.TestProbe
import de.tu_berlin.formic.common.controlalgo.ControlAlgorithm
import de.tu_berlin.formic.common.datatype._
import de.tu_berlin.formic.common.datatype.persistence.AbstractServerDataTypePersistenceSpec._
import de.tu_berlin.formic.common.message.{OperationMessage, UpdateRequest, UpdateResponse}
import de.tu_berlin.formic.common.server.datatype.AbstractServerDataType
import de.tu_berlin.formic.common.{ClientId, DataTypeInstanceId, OperationId}
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
      val id = DataTypeInstanceId()
      val dataType = system.actorOf(Props(new AbstractServerDataTypePersistenceSpec.AbstractServerDataTypePersistenceSpecServerDataType(id, new AbstractServerDataTypePersistenceSpecControlAlgorithm)), id.id)
      val op1 = AbstractServerDataTypePersistenceSpecOperation(OperationId(), OperationContext(), ClientId())
      val op2 = AbstractServerDataTypePersistenceSpecOperation(OperationId(), OperationContext(List(op1.id)), ClientId())
      val msg1 = OperationMessage(ClientId(), id, dataTypeName ,List(op1))
      val msg2 = OperationMessage(ClientId(), id, dataTypeName ,List(op2))

      dataType ! msg1
      dataType ! msg2
      probe.receiveN(2)

      killActors(dataType)

      val recoveredActor = system.actorOf(Props(new AbstractServerDataTypePersistenceSpec.AbstractServerDataTypePersistenceSpecServerDataType(id, new AbstractServerDataTypePersistenceSpecControlAlgorithm)), id.id)

      recoveredActor ! UpdateRequest(ClientId(), id)

      expectMsg(UpdateResponse(id, dataTypeName, "{received}", Some(op2.id)))
    }
  }
}

object AbstractServerDataTypePersistenceSpec {

  val dataTypeName = DataTypeName("persistence")

  case class AbstractServerDataTypePersistenceSpecOperation(id: OperationId, operationContext: OperationContext, var clientId: ClientId) extends DataTypeOperation

  class AbstractServerDataTypePersistenceSpecControlAlgorithm(var canBeApplied: Boolean = true) extends ControlAlgorithm {

    override def canBeApplied(op: DataTypeOperation, history: HistoryBuffer): Boolean = canBeApplied

    override def transform(op: DataTypeOperation, history: HistoryBuffer, transformer: OperationTransformer): DataTypeOperation = op
  }

  class AbstractServerDataTypePersistenceSpecServerDataType(id: DataTypeInstanceId, controlAlgorithm: ControlAlgorithm) extends AbstractServerDataType(id, controlAlgorithm) {

    val transformer = new OperationTransformer {
      override def transform(pair: (DataTypeOperation, DataTypeOperation)): DataTypeOperation = pair._1

      override def bulkTransform(operation: DataTypeOperation, bridge: List[DataTypeOperation]): List[DataTypeOperation] = bridge

      override protected def transformInternal(pair: (DataTypeOperation, DataTypeOperation), withNewContext: Boolean): DataTypeOperation = pair._1
    }

    var data = "{data}"

    override def apply(op: DataTypeOperation): Unit = {
      op match {
        case test: AbstractServerDataTypePersistenceSpecOperation => data = "{received}"
        case _ => fail
      }
    }

    override val dataTypeName: DataTypeName = AbstractServerDataTypePersistenceSpec.dataTypeName

    override def getDataAsJson: String = data
  }

}