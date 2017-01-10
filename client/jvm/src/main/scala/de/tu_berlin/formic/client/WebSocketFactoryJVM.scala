package de.tu_berlin.formic.client
import akka.actor.{ActorRef, ActorSystem}
import akka.stream.ActorMaterializer

import scala.concurrent.ExecutionContext

/**
  * @author Ronny Bräunlich
  */
class WebSocketFactoryJVM()(implicit val materializer: ActorMaterializer, val actorSystem: ActorSystem) extends WebSocketFactory {
  override def createConnection(url: String, connection: ActorRef): WebSocketWrapper = {
    new WrappedAkkaStreamWebSocket(url, connection)(materializer, actorSystem)
  }
}
