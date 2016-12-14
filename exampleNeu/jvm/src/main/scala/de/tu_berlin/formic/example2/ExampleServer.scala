package de.tu_berlin.formic.example2

import akka.stream.{ActorMaterializer, ActorMaterializerSettings}
import de.tu_berlin.formic.server.FormicServer

/**
  * @author Ronny Bräunlich
  */
object ExampleServer {

  def main(args: Array[String]): Unit = {
    implicit val system = FormicServer.system
    implicit val materializer = ActorMaterializer(ActorMaterializerSettings(system))
    FormicServer.start(NetworkRoute.route(FormicServer.newUserProxy))
  }

}
