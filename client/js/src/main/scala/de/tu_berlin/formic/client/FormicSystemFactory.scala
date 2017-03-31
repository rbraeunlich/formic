package de.tu_berlin.formic.client

import com.typesafe.config.Config
import de.tu_berlin.formic.common.datatype.ClientDataStructureProvider

/**
  * @author Ronny Bräunlich
  */
object FormicSystemFactory {

  def create(config: Config, dataTypes: Set[ClientDataStructureProvider]): FormicSystem = new FormicSystem(config, WebSocketFactoryJS) with ClientDataStructures {
    override val dataStructureProvider: Set[ClientDataStructureProvider] = dataTypes
  }
  
}
