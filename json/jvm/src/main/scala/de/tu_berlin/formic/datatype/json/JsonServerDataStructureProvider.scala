package de.tu_berlin.formic.datatype.json

import akka.actor.{ActorRef, ActorSystem, Props}
import de.tu_berlin.formic.common.datatype.{DataStructureName, ServerDataStructureProvider}
import de.tu_berlin.formic.common.json.FormicJsonProtocol

/**
  * @author Ronny Bräunlich
  */
class JsonServerDataStructureProvider extends ServerDataStructureProvider {

  override def initFactories(actorSystem: ActorSystem): Map[DataStructureName, ActorRef] = {
    val factory = actorSystem.actorOf(Props[JsonServerDataStructureFactory], JsonServerDataStructureFactory.name.name)
    Map(JsonServerDataStructureFactory.name -> factory)
  }

  override def registerFormicJsonDataStructureProtocols(formicJsonProtocol: FormicJsonProtocol): Unit = {
    formicJsonProtocol.registerProtocol(new JsonFormicJsonDataStructureProtocol(JsonServerDataStructureFactory.name)(JsonFormicJsonDataStructureProtocol.reader, JsonFormicJsonDataStructureProtocol.writer))
  }
}

object JsonServerDataStructureProvider {
  def apply(): JsonServerDataStructureProvider = new JsonServerDataStructureProvider()
}