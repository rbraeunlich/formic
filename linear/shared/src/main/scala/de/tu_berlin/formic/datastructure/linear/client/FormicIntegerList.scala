package de.tu_berlin.formic.datastructure.linear.client

import akka.actor.ActorRef
import de.tu_berlin.formic.common.{ClientId, DataStructureInstanceId}
import de.tu_berlin.formic.common.datastructure.client.{ClientDataStructureEvent, DataStructureInitiator}
import upickle.default._

import scala.scalajs.js.annotation.JSExport

/**
  * @author Ronny Bräunlich
  */
@JSExport
class FormicIntegerList(callback: (ClientDataStructureEvent) => Unit, initiator: DataStructureInitiator, dataTypeInstanceId: DataStructureInstanceId = DataStructureInstanceId())
  extends FormicList[Int](callback, initiator, dataTypeInstanceId, FormicIntegerListDataStructureFactory.name) {

  def this(callback: (ClientDataStructureEvent) => Unit, initiator: DataStructureInitiator, dataTypeInstanceId: DataStructureInstanceId, wrapped: ActorRef, localClientId: ClientId) {
    this(callback, initiator, dataTypeInstanceId)
    this.actor = wrapped
    this.clientId = localClientId
  }
}
