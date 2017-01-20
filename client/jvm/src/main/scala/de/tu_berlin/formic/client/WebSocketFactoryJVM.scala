package de.tu_berlin.formic.client
import akka.actor.{ActorRef, ActorSystem}
import akka.stream.ActorMaterializer

import scala.concurrent.ExecutionContext

/**
  * @author Ronny Bräunlich
  */
class WebSocketFactoryJVM() extends WebSocketFactory {

  var actorSystem: ActorSystem = _
  var materializer: ActorMaterializer = _

  override def createConnection(url: String, connection: ActorRef): WebSocketWrapper = {
    new WrappedAkkaStreamWebSocket(url, connection)(materializer, actorSystem)
  }
}
