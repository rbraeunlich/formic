package de.tu_berlin.formic.common.datatype

import akka.actor.{ActorRef, ActorSystem}
import de.tu_berlin.formic.common.json.FormicJsonProtocol

/**
  * Trait for modules containing data types. This interface is used to register their
  * factories and FormicJsonProtocols on the client.
  *
  * @author Ronny Bräunlich
  */
trait ClientDataTypeProvider {

  def initFactories(actorSystem: ActorSystem): Map[DataStructureName, ActorRef]

  def registerFormicJsonDataTypeProtocols(formicJsonProtocol: FormicJsonProtocol): Unit

}
