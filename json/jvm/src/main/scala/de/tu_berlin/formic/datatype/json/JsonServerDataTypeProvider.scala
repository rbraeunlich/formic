package de.tu_berlin.formic.datatype.json

import akka.actor.{ActorRef, ActorSystem, Props}
import de.tu_berlin.formic.common.datatype.{DataStructureName, ServerDataTypeProvider}
import de.tu_berlin.formic.common.json.FormicJsonProtocol

/**
  * @author Ronny Bräunlich
  */
class JsonServerDataTypeProvider extends ServerDataTypeProvider {

  override def initFactories(actorSystem: ActorSystem): Map[DataStructureName, ActorRef] = {
    val factory = actorSystem.actorOf(Props[JsonServerDataTypeFactory], JsonServerDataTypeFactory.name.name)
    Map(JsonServerDataTypeFactory.name -> factory)
  }

  override def registerFormicJsonDataTypeProtocols(formicJsonProtocol: FormicJsonProtocol): Unit = {
    formicJsonProtocol.registerProtocol(new JsonFormicJsonDataTypeProtocol(JsonServerDataTypeFactory.name)(JsonFormicJsonDataTypeProtocol.reader, JsonFormicJsonDataTypeProtocol.writer))
  }
}

object JsonServerDataTypeProvider {
  def apply(): JsonServerDataTypeProvider = new JsonServerDataTypeProvider()
}