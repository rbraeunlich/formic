package de.tu_berlin.formic.common.datatype

import akka.actor.{ActorRef, ActorSystem}
import de.tu_berlin.formic.common.json.FormicJsonProtocol

/**
  * Trait for modules containing data structures. This interface is used to register their
  * factories and FormicJsonProtocols on the server.
  * @author Ronny Bräunlich
  */
trait ServerDataStructureProvider {

  def initFactories(actorSystem: ActorSystem): Map[DataStructureName, ActorRef]

  def registerFormicJsonDataStructureProtocols(formicJsonProtocol: FormicJsonProtocol): Unit

}
