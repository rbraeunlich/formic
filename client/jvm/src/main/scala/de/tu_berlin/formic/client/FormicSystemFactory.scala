package de.tu_berlin.formic.client

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.typesafe.config.Config
import de.tu_berlin.formic.common.datatype.ClientDataTypeProvider

/**
  * @author Ronny Bräunlich
  */
object FormicSystemFactory {

  def create(config: Config, dataTypes: Set[ClientDataTypeProvider])(implicit materializer: ActorMaterializer, actorSystem: ActorSystem): FormicSystem = new FormicSystem(config, new WebSocketFactoryJVM()) with ClientDataTypes {
    override val dataTypeProvider: Set[ClientDataTypeProvider] = dataTypes
  }

}
