package de.tu_berlin.formic.common.datatype.persistence

import akka.actor.{ActorSystem, Props}
import de.tu_berlin.formic.common.controlalgo.{ControlAlgorithm, WaveOTServer}
import de.tu_berlin.formic.common.{ClientId, DataStructureInstanceId$, OperationId}
import de.tu_berlin.formic.common.datatype._
import de.tu_berlin.formic.common.datatype.persistence.AbstractServerDataTypeFactoryPersistenceSpec.AbstractServerDataTypeFactoryPersistenceSpecFactory
import de.tu_berlin.formic.common.message.CreateRequest
import de.tu_berlin.formic.common.server.datatype.{AbstractServerDataType, AbstractServerDataTypeFactory}
import org.scalatest.Assertions._

import scala.concurrent.Await
import scala.concurrent.duration._

/**
  * @author Ronny Bräunlich
  */
class AbstractServerDataTypeFactoryPersistenceSpec extends PersistenceSpec(ActorSystem("AbstractServerDataTypeFactoryPersistenceSpec"))
  with PersistenceCleanup {

  "An AbstractServerDataTypeFactory" should {
    "re-apply stored operations after recovery" in {
      val factory = system.actorOf(Props(new AbstractServerDataTypeFactoryPersistenceSpecFactory), AbstractServerDataTypeFactoryPersistenceSpec.dataTypeName.name)
      val dataTypeInstanceId = DataStructureInstanceId()
      val dataTypeInstanceId2 = DataStructureInstanceId()
      factory ! CreateRequest(ClientId(), dataTypeInstanceId, AbstractServerDataTypeFactoryPersistenceSpec.dataTypeName)
      factory ! CreateRequest(ClientId(), dataTypeInstanceId2, AbstractServerDataTypeFactoryPersistenceSpec.dataTypeName)
      receiveN(2)

      killActors(factory)

      val recoveredFactory = system.actorOf(Props(new AbstractServerDataTypeFactoryPersistenceSpecFactory), AbstractServerDataTypeFactoryPersistenceSpec.dataTypeName.name)
      Thread.sleep(2000)
      val dataType = system.actorSelection(recoveredFactory.path.child(dataTypeInstanceId.id)).resolveOne(5.seconds)
      Await.result(dataType, 5.seconds) shouldNot be(null)
      val dataType2 = system.actorSelection(recoveredFactory.path.child(dataTypeInstanceId2.id)).resolveOne(5.seconds)
      Await.result(dataType2, 5.seconds) shouldNot be(null)
    }
  }

}

object AbstractServerDataTypeFactoryPersistenceSpec {

  val dataTypeName = DataTypeName("persistenceFactory")

  class AbstractServerDataTypeFactoryPersistenceSpecServerDataType(id: DataStructureInstanceId, controlAlgorithm: ControlAlgorithm) extends AbstractServerDataType(id, controlAlgorithm) {

    val transformer = new OperationTransformer {
      override def transform(pair: (DataTypeOperation, DataTypeOperation)): DataTypeOperation = pair._1

      override def bulkTransform(operation: DataTypeOperation, bridge: List[DataTypeOperation]): List[DataTypeOperation] = bridge

      override protected def transformInternal(pair: (DataTypeOperation, DataTypeOperation), withNewContext: Boolean): DataTypeOperation = pair._1
    }

    var data = ""

    override def apply(op: DataTypeOperation): Unit = {
      op match {
        case _ => fail
      }
    }

    override val dataTypeName: DataTypeName = AbstractServerDataTypeFactoryPersistenceSpec.dataTypeName

    override def getDataAsJson: String = data
  }

  class AbstractServerDataTypeFactoryPersistenceSpecFactory
    extends AbstractServerDataTypeFactory[AbstractServerDataTypeFactoryPersistenceSpecServerDataType] {
    override def create(dataTypeInstanceId: DataStructureInstanceId): AbstractServerDataTypeFactoryPersistenceSpecServerDataType = {
      new AbstractServerDataTypeFactoryPersistenceSpecServerDataType(dataTypeInstanceId, new WaveOTServer())
    }

    override val name: DataTypeName = AbstractServerDataTypeFactoryPersistenceSpec.dataTypeName
  }

}
