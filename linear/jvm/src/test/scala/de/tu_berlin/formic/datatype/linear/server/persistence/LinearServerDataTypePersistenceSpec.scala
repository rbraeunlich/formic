package de.tu_berlin.formic.datatype.linear.server.persistence

import akka.actor.{ActorSystem, Props}
import akka.testkit.TestProbe
import de.tu_berlin.formic.common.controlalgo.WaveOTServer
import de.tu_berlin.formic.common.datatype.{DataTypeName, OperationContext}
import de.tu_berlin.formic.common.message.{OperationMessage, UpdateRequest, UpdateResponse}
import de.tu_berlin.formic.common.{ClientId, DataTypeInstanceId, OperationId}
import de.tu_berlin.formic.datatype.linear.server.{IntegerListDataTypeFactory, LinearServerDataType}
import de.tu_berlin.formic.datatype.linear.{LinearDeleteOperation, LinearInsertOperation, LinearNoOperation}

import scala.concurrent.duration._
/**
  * @author Ronny Bräunlich
  */
class LinearServerDataTypePersistenceSpec extends PersistenceSpec(ActorSystem("LinearServerDataTypePersistenceSpec"))
  with PersistenceCleanup {

  "An LinearServerDataType" should {
    "re-apply stored operations after recovery" in {
      val probe = TestProbe()
      system.eventStream.subscribe(probe.ref, classOf[OperationMessage])
      val id = DataTypeInstanceId()
      val dataTypeName: DataTypeName = IntegerListDataTypeFactory.name
      val dataType = system.actorOf(Props(new LinearServerDataType[Int](id, new WaveOTServer(), dataTypeName)), id.id)
      val op1 = LinearInsertOperation(0, 1, OperationId(), OperationContext(), ClientId())
      val op2 = LinearInsertOperation(1, 2, OperationId(), OperationContext(List(op1.id)), ClientId())
      val op3 = LinearDeleteOperation(1, OperationId(), OperationContext(List(op2.id)), ClientId())
      val op4 = LinearNoOperation(OperationId(), OperationContext(List(op3.id)), ClientId())
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

      val recoveredActor = system.actorOf(Props(new LinearServerDataType[Int](id, new WaveOTServer(), dataTypeName)), id.id)

      recoveredActor ! UpdateRequest(ClientId(), id)

      expectMsg(UpdateResponse(id, dataTypeName, "[1]", Some(op4.id)))
    }
  }
}