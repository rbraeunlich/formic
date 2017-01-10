package de.tu_berlin.formic.example

import akka.http.scaladsl.Http

/**
  * @author Ronny Bräunlich
  */
class ServerThread extends Thread {

  val exampleServer = new ExampleServer

  var serverBinding: Http.ServerBinding = _

  override def run() {
    serverBinding = exampleServer.start()
  }

  def terminate(): Unit = {
    exampleServer.shutdown()
  }
}
