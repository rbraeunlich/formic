package de.tu_berlin.formic.client

import org.scalajs.dom
import org.scalajs.dom.WebSocket

/**
  * @author Ronny Bräunlich
  */
trait WebSocketFactory {

  def createConnection(url: String): dom.WebSocket

}

object WebSocketFactory extends WebSocketFactory {

  override def createConnection(url: String): WebSocket = new WebSocket(url)

}