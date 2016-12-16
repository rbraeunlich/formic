package de.tu_berlin.formic.server

import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route

/**
  * @author Ronny Bräunlich
  */
class ServerThread(val server: FormicServer, val route: Route) extends Thread {

  var binding: Http.ServerBinding = _

  override def run() {
    binding = server.start(route)
  }

  def terminate(): Unit = {
    server.terminate()
  }
}