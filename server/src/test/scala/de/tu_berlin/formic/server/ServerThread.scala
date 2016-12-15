package de.tu_berlin.formic.server

import akka.http.scaladsl.server.Route

/**
  * @author Ronny Bräunlich
  */
class ServerThread(val server: FormicServer, val route: Route) extends Thread {

  override def run() {
    server.start(route)
  }

  def terminate(): Unit = {
    server.terminate()
  }
}