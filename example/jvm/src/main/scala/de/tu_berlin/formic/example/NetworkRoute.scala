package de.tu_berlin.formic.example

import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.scaladsl.model.StatusCodes
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
    path("formic") {
      authenticateBasic[String]("FormicRealm", (creds) => UniqueUsernameAuthenticator(actorSystem).authenticate(creds)) {
        identifier =>
          get {
            handleWebSocketMessages(newUserMethod(identifier))
          }
      }
    } ~
    pathEndOrSingleSlash {
      redirect("/index", StatusCodes.PermanentRedirect)
    } ~
    path("index") {
      getFromResource("index.html")
    } ~
      getFromResourceDirectory("") //this route is needed to serve the JavaScript files to clients
  }
}
