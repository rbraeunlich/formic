package de.tu_berlin.formic.datatype.tree.persistence

import akka.actor.{ActorSystem, Props}
import akka.testkit.TestProbe
import de.tu_berlin.formic.common.controlalgo.WaveOTServer
import de.tu_berlin.formic.common.datatype.{DataTypeName, OperationContext}
import de.tu_berlin.formic.common.message.{OperationMessage, UpdateRequest, UpdateResponse}
import de.tu_berlin.formic.common.{ClientId, DataTypeInstanceId, OperationId}
import de.tu_berlin.formic.datatype.tree._
import scala.concurrent.duration._

/**
  * @author Ronny Bräunlich
  */
class TreeServerDataTypePersistenceSpec extends PersistenceSpec(ActorSystem("TreeServerDataTypePersistenceSpec"))
  with PersistenceCleanup {

  "An TreeServerDataType" should {
    "re-apply stored operations after recovery" in {
      val probe = TestProbe()
      system.eventStream.subscribe(probe.ref, classOf[OperationMessage])
      val id = DataTypeInstanceId()
      val dataTypeName: DataTypeName = StringTreeDataTypeFactory.name
      val dataType = system.actorOf(Props(new TreeServerDataType[String](id, new WaveOTServer(), dataTypeName)), id.id)
      val op1 = TreeInsertOperation(AccessPath(), ValueTreeNode("root"), OperationId(), OperationContext(), ClientId())
      val op2 = TreeInsertOperation(AccessPath(0), ValueTreeNode("child"), OperationId(), OperationContext(List(op1.id)), ClientId())
      val op3 = TreeDeleteOperation(AccessPath(0), OperationId(), OperationContext(List(op2.id)), ClientId())
      val op4 = TreeNoOperation(OperationId(), OperationContext(List(op3.id)), ClientId())
      val msg1 = OperationMessage(ClientId(), id, dataTypeName, List(op1))
      val msg2 = OperationMessage(ClientId(), id, dataTypeName, List(op2))
      val msg3 = OperationMessage(ClientId(), id, dataTypeName, List(op3))
      val msg4 = OperationMessage(ClientId(), id, dataTypeName, List(op4))

      dataType ! msg1
      dataType ! msg2
      dataType ! msg3
      dataType ! msg4
      probe.receiveN(4, 5.seconds)

      killActors(dataType)

      val recoveredActor = system.actorOf(Props(new TreeServerDataType[String](id, new WaveOTServer(), dataTypeName)), id.id)

      recoveredActor ! UpdateRequest(ClientId(), id)

      expectMsg(UpdateResponse(id, dataTypeName, """{"value":"root","children":[]}""", Some(op4.id)))
    }
  }
}
