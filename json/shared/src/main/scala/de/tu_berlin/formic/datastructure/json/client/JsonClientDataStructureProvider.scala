package de.tu_berlin.formic.datastructure.json.client

import akka.actor.{ActorRef, ActorSystem, Props}
import de.tu_berlin.formic.common.datastructure.{ClientDataStructureProvider, DataStructureName}
import de.tu_berlin.formic.common.json.FormicJsonProtocol
import de.tu_berlin.formic.datastructure.json.JsonFormicJsonDataStructureProtocol

/**
  * @author Ronny Bräunlich
  */
class JsonClientDataStructureProvider extends ClientDataStructureProvider {

  override def initFactories(actorSystem: ActorSystem): Map[DataStructureName, ActorRef] = {
    val factory = actorSystem.actorOf(Props(new FormicJsonObjectFactory), FormicJsonObjectFactory.name.name)
    Map(FormicJsonObjectFactory.name -> factory)
  }

  override def registerFormicJsonDataStructureProtocols(formicJsonProtocol: FormicJsonProtocol): Unit = {
    formicJsonProtocol.registerProtocol(new JsonFormicJsonDataStructureProtocol(FormicJsonObjectFactory.name)(JsonFormicJsonDataStructureProtocol.reader, JsonFormicJsonDataStructureProtocol.writer))
  }
}

object JsonClientDataStructureProvider {
  def apply(): JsonClientDataStructureProvider = new JsonClientDataStructureProvider()
}