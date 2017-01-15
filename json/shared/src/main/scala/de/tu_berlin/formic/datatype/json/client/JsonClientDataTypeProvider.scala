package de.tu_berlin.formic.datatype.json.client

import akka.actor.{ActorRef, ActorSystem, Props}
import de.tu_berlin.formic.common.datatype.{ClientDataTypeProvider, DataTypeName}
import de.tu_berlin.formic.common.json.FormicJsonProtocol
import de.tu_berlin.formic.datatype.json.JsonFormicJsonDataTypeProtocol

/**
  * @author Ronny Bräunlich
  */
class JsonClientDataTypeProvider extends ClientDataTypeProvider {

  override def initFactories(actorSystem: ActorSystem): Map[DataTypeName, ActorRef] = {
    val factory = actorSystem.actorOf(Props(new FormicJsonObjectFactory), FormicJsonObjectFactory.name.name)
    Map(FormicJsonObjectFactory.name -> factory)
  }

  override def registerFormicJsonDataTypeProtocols(formicJsonProtocol: FormicJsonProtocol): Unit = {
    formicJsonProtocol.registerProtocol(new JsonFormicJsonDataTypeProtocol(FormicJsonObjectFactory.name)(JsonFormicJsonDataTypeProtocol.reader, JsonFormicJsonDataTypeProtocol.writer))
  }
}

object JsonClientDataTypeProvider {
  def apply(): JsonClientDataTypeProvider = new JsonClientDataTypeProvider()
}