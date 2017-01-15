package de.tu_berlin.formic.client

import com.typesafe.config.Config
import de.tu_berlin.formic.common.datatype.ClientDataTypeProvider

/**
  * @author Ronny Bräunlich
  */
object FormicSystemFactory {

  def create(config: Config, dataTypes: Set[ClientDataTypeProvider]): FormicSystem = new FormicSystem(config, WebSocketFactoryJS) with ClientDataTypes {
    override val dataTypeProvider: Set[ClientDataTypeProvider] = dataTypes
  }
  
}
