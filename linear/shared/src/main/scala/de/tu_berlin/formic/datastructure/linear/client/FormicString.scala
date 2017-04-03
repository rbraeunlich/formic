package de.tu_berlin.formic.datastructure.linear.client

import akka.actor.ActorRef
import de.tu_berlin.formic.common.datastructure.client.{ClientDataStructureEvent, DataStructureInitiator}
import de.tu_berlin.formic.common.{ClientId, DataStructureInstanceId}
import upickle.default._

import scala.scalajs.js.annotation.JSExport
/**
  * @author Ronny Bräunlich
  */
@JSExport
class FormicString(callback: (ClientDataStructureEvent) => Unit, initiator: DataStructureInitiator, dataTypeInstanceId: DataStructureInstanceId = DataStructureInstanceId())
  extends FormicList[Char](callback, initiator, dataTypeInstanceId, FormicStringDataStructureFactory.name) {

  def this(callback: (ClientDataStructureEvent) => Unit, initiator: DataStructureInitiator, dataTypeInstanceId: DataStructureInstanceId, wrapped: ActorRef, localClientId: ClientId) {
    this(callback, initiator, dataTypeInstanceId)
    this.actor = wrapped
    this.clientId = localClientId
  }
}
