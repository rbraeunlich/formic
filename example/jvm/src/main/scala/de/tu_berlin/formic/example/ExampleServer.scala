package de.tu_berlin.formic.example

import akka.stream.{ActorMaterializer, ActorMaterializerSettings}
import de.tu_berlin.formic.server.FormicServer

/**
  * @author Ronny Bräunlich
  */
object ExampleServer {

  val server = new FormicServer

  def main(args: Array[String]): Unit = {
    implicit val system = server.system
    implicit val materializer = server.materializer
    server.start(new NetworkRoute().route(server.newUserProxy))
  }

  def shutdown = server.terminate()
}
