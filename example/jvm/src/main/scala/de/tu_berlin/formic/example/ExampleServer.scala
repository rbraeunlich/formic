package de.tu_berlin.formic.example

import akka.stream.{ActorMaterializer, ActorMaterializerSettings}
import de.tu_berlin.formic.server.FormicServer

/**
  * @author Ronny Bräunlich
  */
object ExampleServer {

  def main(args: Array[String]): Unit = {
    val server = new FormicServer
    implicit val system = server.system
    implicit val materializer = server.materializer
    server.start(NetworkRoute.route(server.newUserProxy))
  }

}
