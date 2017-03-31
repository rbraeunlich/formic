package de.tu_berlin.formic.datatype.json.persistence

import akka.actor.{ActorSystem, Props}
import akka.testkit.TestProbe
import de.tu_berlin.formic.common.{ClientId, DataStructureInstanceId, OperationId}
import de.tu_berlin.formic.common.controlalgo.WaveOTServer
import de.tu_berlin.formic.common.datatype.{DataStructureName, OperationContext}
import de.tu_berlin.formic.common.message.{OperationMessage, UpdateRequest, UpdateResponse}
import de.tu_berlin.formic.datatype.json._
import de.tu_berlin.formic.datatype.tree._
import scala.concurrent.duration._

/**
  * @author Ronny Bräunlich
  */
class JsonServerDataStructurePersistenceSpec extends PersistenceSpec(ActorSystem("JsonServerDataStructurePersistenceSpec"))
  with PersistenceCleanup {

  "An JsonServerDataStructure" should {
    "re-apply stored operations after recovery" in {
      val probe = TestProbe()
      system.eventStream.subscribe(probe.ref, classOf[OperationMessage])
      val id = DataStructureInstanceId()
      val dataTypeName: DataStructureName = JsonServerDataStructureFactory.name
      val dataType = system.actorOf(Props(new JsonServerDataStructure(id, new WaveOTServer(), dataTypeName)), id.id)
      val op1 = TreeInsertOperation(AccessPath(0), BooleanNode("bool", value = true), OperationId(), OperationContext(), ClientId())
      val op2 = TreeInsertOperation(AccessPath(1), NumberNode("num", 12), OperationId(), OperationContext(List(op1.id)), ClientId())
      val op3 = TreeDeleteOperation(AccessPath(0), OperationId(), OperationContext(List(op2.id)), ClientId())
      val op4 = TreeNoOperation(OperationId(), OperationContext(List(op3.id)), ClientId())
      val op5 = JsonReplaceOperation(AccessPath(0), NumberNode("num", 13), OperationId(), OperationContext(List(op4.id)), ClientId())
      val msg1 = OperationMessage(ClientId(), id, dataTypeName, List(op1))
      val msg2 = OperationMessage(ClientId(), id, dataTypeName, List(op2))
      val msg3 = OperationMessage(ClientId(), id, dataTypeName, List(op3))
      val msg4 = OperationMessage(ClientId(), id, dataTypeName, List(op4))
      val msg5 = OperationMessage(ClientId(), id, dataTypeName, List(op5))

      dataType ! msg1
      dataType ! msg2
      dataType ! msg3
      dataType ! msg4
      dataType ! msg5
      probe.receiveN(5, 6.seconds)

      killActors(dataType)

      val recoveredActor = system.actorOf(Props(new JsonServerDataStructure(id, new WaveOTServer(), dataTypeName)), id.id)

      recoveredActor ! UpdateRequest(ClientId(), id)

      expectMsg(UpdateResponse(id, dataTypeName, """{"num":13}""", Some(op5.id)))
    }
  }
}