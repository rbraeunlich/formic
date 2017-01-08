package de.tu_berlin.formic.client

/**
  * Wrapper class to hide the actual WebSocket connection. That ways JavaScript-
  * and JVM-based connections can be treated equally.
  *
  * @author Ronny Bräunlich
  */
trait WebSocketWrapper {

  def send(message: String)

}
