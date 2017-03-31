package de.tu_berlin.formic.example

import akka.http.scaladsl.Http
import de.tu_berlin.formic.server.FormicServer

/**
  * @author Ronny Bräunlich
  */
class ExampleServer {

  val server = new FormicServer with ExampleServerDataStructures

  def start(): Http.ServerBinding = {
    implicit val system = server.system
    implicit val materializer = server.materializer
    server.start(new NetworkRoute().route(server.newUserProxy))
  }

  def shutdown() = server.terminate()
}

object ExampleServer {

  val exampleServer = new ExampleServer

  def main(args: Array[String]): Unit = {
    exampleServer.start()
  }
}