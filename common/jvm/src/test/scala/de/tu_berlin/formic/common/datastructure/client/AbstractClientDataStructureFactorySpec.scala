package de.tu_berlin.formic.common.datastructure.client

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import de.tu_berlin.formic.common.controlalgo.ControlAlgorithmClient
import de.tu_berlin.formic.common.datastructure._
import de.tu_berlin.formic.common.datastructure.client.AbstractClientDataStructureFactory.{LocalCreateRequest, NewDataStructureCreated, WrappedCreateRequest}
import de.tu_berlin.formic.common.message.CreateRequest
import de.tu_berlin.formic.common.{ClientId, DataStructureInstanceId, OperationId}
import org.scalatest.{Matchers, WordSpecLike}

/**
  * @author Ronny Bräunlich
  */
class AbstractClientDataStructureFactorySpec extends TestKit(ActorSystem("AbstractClientDataStructureFactorySpec"))
  with WordSpecLike
  with ImplicitSender
  with StopSystemAfterAll
  with Matchers {

  "AbstractClientDataStructureFactorySpec" must {

    "create a data type and data type wrapper when receiving a WrappedCreateRequest" in {
      val factory = system.actorOf(Props(new AbstractClientDataStructureFactorySpecFactory))
      val outgoing = TestProbe()
      val instanceId = DataStructureInstanceId()
      val clientId = ClientId()

      factory ! WrappedCreateRequest(outgoing.ref, null, Option.empty,CreateRequest(ClientId(), instanceId, DataStructureName("AbstractClientDataTypeFactorySpec") ), clientId)

      val msg = expectMsgClass(classOf[NewDataStructureCreated])
      msg.dataStructureInstanceId should equal(instanceId)
      msg.dataStructureActor should not equal null
      msg.wrapper shouldBe a[AbstractClientDataStructureFactorySpecFormicDataStructure]
      msg.wrapper.clientId should equal(clientId)
    }

    "create only a data type when receiving a LocalCreateRequest" in {
      val factory = system.actorOf(Props(new AbstractClientDataStructureFactorySpecFactory))
      val outgoing = TestProbe()
      val instanceId = DataStructureInstanceId()

      factory ! LocalCreateRequest(outgoing.ref, instanceId)

      val msg = expectMsgClass(classOf[NewDataStructureCreated])
      msg.dataStructureInstanceId should equal(instanceId)
      msg.dataStructureActor should not equal null
      msg.wrapper should be(null)
    }

  }

}

object AbstractClientDataStructureSpecControlAlgorithm extends ControlAlgorithmClient {

  override def canLocalOperationBeApplied(op: DataStructureOperation): Boolean = true

  override def canBeApplied(op: DataStructureOperation, history: HistoryBuffer): Boolean = true

  override def transform(op: DataStructureOperation, history: HistoryBuffer, transformer: OperationTransformer): DataStructureOperation = op

  override def currentOperationContext: OperationContext = OperationContext(List.empty)
}

class AbstractClientDataStructureFactorySpecServerDataStructure(outgoingConnection: ActorRef) extends AbstractClientDataStructure(DataStructureInstanceId(), AbstractClientDataStructureSpecControlAlgorithm, Option.empty, outgoingConnection) {
  override val dataStructureName: DataStructureName = DataStructureName("AbstractClientDataTypeFactorySpec")

  override val transformer: OperationTransformer = null

  override def apply(op: DataStructureOperation): Unit = {}

  override def getDataAsJson: String = ""

  override def cloneOperationWithNewContext(op: DataStructureOperation, context: OperationContext): DataStructureOperation = op
}

class AbstractClientDataStructureFactorySpecFormicDataStructure(clientId: ClientId) extends FormicDataStructure(null, DataStructureName("AbstractClientDataTypeFactorySpec"),null,clientId, DataStructureInstanceId(), new DataStructureInitiator {
  override def initDataStructure(dataType: FormicDataStructure): Unit = {}
}) {
}

class AbstractClientDataStructureFactorySpecFactory extends AbstractClientDataStructureFactory[AbstractClientDataStructureFactorySpecServerDataStructure, AbstractClientDataStructureFactorySpecFormicDataStructure] {

  override val name: DataStructureName = DataStructureName("AbstractClientDataTypeFactorySpec")

  override def createDataStructure(dataTypeInstanceId: DataStructureInstanceId, outgoingConnection: ActorRef, data: Option[String], lastOperationId: Option[OperationId]): AbstractClientDataStructureFactorySpecServerDataStructure = {
    new AbstractClientDataStructureFactorySpecServerDataStructure(outgoingConnection)
  }

  override def createWrapper(dataTypeInstanceId: DataStructureInstanceId, dataType: ActorRef, localClientId: ClientId): AbstractClientDataStructureFactorySpecFormicDataStructure = {
    new AbstractClientDataStructureFactorySpecFormicDataStructure(localClientId)
  }
}