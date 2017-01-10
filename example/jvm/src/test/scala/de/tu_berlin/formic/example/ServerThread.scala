package de.tu_berlin.formic.example

/**
  * @author Ronny Bräunlich
  */
class ServerThread extends Thread {

  override def run() {
    ExampleServer.main(Array.empty)
  }

  def terminate(): Unit = {
    ExampleServer.shutdown()
  }
}
