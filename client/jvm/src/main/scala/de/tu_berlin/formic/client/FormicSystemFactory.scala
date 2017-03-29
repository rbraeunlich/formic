package de.tu_berlin.formic.client

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, ActorMaterializerSettings}
import com.typesafe.config.Config
import de.tu_berlin.formic.common.datatype.ClientDataStructureProvider

/**
  * @author Ronny Bräunlich
  */
object FormicSystemFactory {

  def create(config: Config, dataTypes: Set[ClientDataStructureProvider]): FormicSystem = {
    val webSocketFactory = new WebSocketFactoryJVM()
    val system = new FormicSystem(config, webSocketFactory) with ClientDataTypes {
      override val dataTypeProvider: Set[ClientDataStructureProvider] = dataTypes
    }
    //unfortunately, there is no other way
    implicit val actorSystem = system.system
    webSocketFactory.actorSystem = actorSystem
    webSocketFactory.materializer = ActorMaterializer(ActorMaterializerSettings(system.system))
    system
  }

}
