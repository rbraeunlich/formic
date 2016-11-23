package de.tu_berlin.formic.server

import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.scaladsl.model.ws.Message
import akka.http.scaladsl.server
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Flow

/**
  * @author Ronny Bräunlich
  */
object NetworkRoute {
  def route(newUserMethod: (String) => Flow[Message, Message, NotUsed])(implicit actorSystem: ActorSystem, materializer: ActorMaterializer): server.Route = {
    authenticateBasic[String]("FormicRealm", (creds) => UniqueUsernameAuthenticator.authenticate(creds)) {
      identifier =>
        path("formic") {
          handleWebSocketMessages(newUserMethod(identifier))
        }
    }
  }
}
