package de.tu_berlin.formic.common.datatype

import akka.actor.{ActorRef, ActorSystem}
import de.tu_berlin.formic.common.json.FormicJsonProtocol

/**
  * @author Ronny Bräunlich
  */
trait ServerDataTypeProvider {

  def initFactories(actorSystem: ActorSystem): Map[DataTypeName, ActorRef]

  def registerFormicJsonDataTypeProtocols(formicJsonProtocol: FormicJsonProtocol): Unit

}
