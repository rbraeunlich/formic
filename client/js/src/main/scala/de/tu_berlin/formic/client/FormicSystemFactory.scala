package de.tu_berlin.formic.client

import com.typesafe.config.Config

/**
  * @author Ronny Bräunlich
  */
object FormicSystemFactory {

  def create(config: Config): FormicSystem = new FormicSystem(config, WebSocketFactoryJS)
  
}
