package de.tu_berlin.formic.datatype.json.client

import akka.actor.{ActorRef, ActorSystem, Props}
import de.tu_berlin.formic.common.datatype.{ClientDataStructureProvider, DataStructureName}
import de.tu_berlin.formic.common.json.FormicJsonProtocol
import de.tu_berlin.formic.datatype.json.JsonFormicJsonDataTypeProtocol

/**
  * @author Ronny Bräunlich
  */
class JsonClientDataStructureProvider extends ClientDataStructureProvider {

  override def initFactories(actorSystem: ActorSystem): Map[DataStructureName, ActorRef] = {
    val factory = actorSystem.actorOf(Props(new FormicJsonObjectFactory), FormicJsonObjectFactory.name.name)
    Map(FormicJsonObjectFactory.name -> factory)
  }

  override def registerFormicJsonDataStructureProtocols(formicJsonProtocol: FormicJsonProtocol): Unit = {
    formicJsonProtocol.registerProtocol(new JsonFormicJsonDataTypeProtocol(FormicJsonObjectFactory.name)(JsonFormicJsonDataTypeProtocol.reader, JsonFormicJsonDataTypeProtocol.writer))
  }
}

object JsonClientDataStructureProvider {
  def apply(): JsonClientDataStructureProvider = new JsonClientDataStructureProvider()
}