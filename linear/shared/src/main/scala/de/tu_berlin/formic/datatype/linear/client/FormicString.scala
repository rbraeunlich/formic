package de.tu_berlin.formic.datatype.linear.client

import akka.actor.ActorRef
import de.tu_berlin.formic.common.{ClientId, DataStructureInstanceId}
import de.tu_berlin.formic.common.datatype.client.{ClientDataTypeEvent, DataStructureInitiator}
import upickle.default._

import scala.scalajs.js.annotation.JSExport
/**
  * @author Ronny Bräunlich
  */
@JSExport
class FormicString(callback: (ClientDataTypeEvent) => Unit, initiator: DataStructureInitiator, dataTypeInstanceId: DataStructureInstanceId = DataStructureInstanceId())
  extends FormicList[Char](callback, initiator, dataTypeInstanceId, FormicStringDataTypeFactory.name) {

  def this(callback: (ClientDataTypeEvent) => Unit, initiator: DataStructureInitiator, dataTypeInstanceId: DataStructureInstanceId, wrapped: ActorRef, localClientId: ClientId) {
    this(callback, initiator, dataTypeInstanceId)
    this.actor = wrapped
    this.clientId = localClientId
  }
}
