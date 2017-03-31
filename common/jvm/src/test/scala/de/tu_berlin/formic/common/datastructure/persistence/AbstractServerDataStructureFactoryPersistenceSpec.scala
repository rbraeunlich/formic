package de.tu_berlin.formic.common.datastructure.persistence

import akka.actor.{ActorSystem, Props}
import de.tu_berlin.formic.common.controlalgo.{ControlAlgorithm, WaveOTServer}
import de.tu_berlin.formic.common.{ClientId, DataStructureInstanceId, OperationId}
import de.tu_berlin.formic.common.datastructure._
import de.tu_berlin.formic.common.datastructure.persistence.AbstractServerDataStructureFactoryPersistenceSpec.AbstractServerDataStructureFactoryPersistenceSpecFactory
import de.tu_berlin.formic.common.message.CreateRequest
import de.tu_berlin.formic.common.server.datastructure.{AbstractServerDataStructure, AbstractServerDataStructureFactory}
import org.scalatest.Assertions._

import scala.concurrent.Await
import scala.concurrent.duration._

/**
  * @author Ronny Bräunlich
  */
class AbstractServerDataStructureFactoryPersistenceSpec extends PersistenceSpec(ActorSystem("AbstractServerDataStructureFactoryPersistenceSpec"))
  with PersistenceCleanup {

  "An AbstractServerDataStructureFactoryPersistenceSpec" should {
    "re-apply stored operations after recovery" in {
      val factory = system.actorOf(Props(new AbstractServerDataStructureFactoryPersistenceSpecFactory), AbstractServerDataStructureFactoryPersistenceSpec.dataTypeName.name)
      val dataTypeInstanceId = DataStructureInstanceId()
      val dataTypeInstanceId2 = DataStructureInstanceId()
      factory ! CreateRequest(ClientId(), dataTypeInstanceId, AbstractServerDataStructureFactoryPersistenceSpec.dataTypeName)
      factory ! CreateRequest(ClientId(), dataTypeInstanceId2, AbstractServerDataStructureFactoryPersistenceSpec.dataTypeName)
      receiveN(2)

      killActors(factory)

      val recoveredFactory = system.actorOf(Props(new AbstractServerDataStructureFactoryPersistenceSpecFactory), AbstractServerDataStructureFactoryPersistenceSpec.dataTypeName.name)
      Thread.sleep(2000)
      val dataType = system.actorSelection(recoveredFactory.path.child(dataTypeInstanceId.id)).resolveOne(5.seconds)
      Await.result(dataType, 5.seconds) shouldNot be(null)
      val dataType2 = system.actorSelection(recoveredFactory.path.child(dataTypeInstanceId2.id)).resolveOne(5.seconds)
      Await.result(dataType2, 5.seconds) shouldNot be(null)
    }
  }

}

object AbstractServerDataStructureFactoryPersistenceSpec {

  val dataTypeName = DataStructureName("persistenceFactory")

  class AbstractServerDataStructureFactoryPersistenceSpecServerDataStructure(id: DataStructureInstanceId, controlAlgorithm: ControlAlgorithm) extends AbstractServerDataStructure(id, controlAlgorithm) {

    val transformer = new OperationTransformer {
      override def transform(pair: (DataStructureOperation, DataStructureOperation)): DataStructureOperation = pair._1

      override def bulkTransform(operation: DataStructureOperation, bridge: List[DataStructureOperation]): List[DataStructureOperation] = bridge

      override protected def transformInternal(pair: (DataStructureOperation, DataStructureOperation), withNewContext: Boolean): DataStructureOperation = pair._1
    }

    var data = ""

    override def apply(op: DataStructureOperation): Unit = {
      op match {
        case _ => fail
      }
    }

    override val dataTypeName: DataStructureName = AbstractServerDataStructureFactoryPersistenceSpec.dataTypeName

    override def getDataAsJson: String = data
  }

  class AbstractServerDataStructureFactoryPersistenceSpecFactory
    extends AbstractServerDataStructureFactory[AbstractServerDataStructureFactoryPersistenceSpecServerDataStructure] {
    override def create(dataTypeInstanceId: DataStructureInstanceId): AbstractServerDataStructureFactoryPersistenceSpecServerDataStructure = {
      new AbstractServerDataStructureFactoryPersistenceSpecServerDataStructure(dataTypeInstanceId, new WaveOTServer())
    }

    override val name: DataStructureName = AbstractServerDataStructureFactoryPersistenceSpec.dataTypeName
  }

}
