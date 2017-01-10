package de.tu_berlin.formic.client

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.typesafe.config.Config

import scala.concurrent.ExecutionContext

/**
  * @author Ronny Bräunlich
  */
object FormicSystemFactory {

  def create(config: Config)(implicit materializer: ActorMaterializer, actorSystem: ActorSystem): FormicSystem = new FormicSystem(config, new WebSocketFactoryJVM())

}
