package de.tu_berlin.formic.client

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

/**
  * @author Ronny Bräunlich
  */
@js.native
@JSImport("mock-socket", "Server")
class Server(url: String) extends js.Object {

  def start(): Nothing = js.native

  def stop(callback: js.Function0[Unit]): Nothing = js.native

  /*
    * @param {string} type - The event key to subscribe to. Valid keys are: connection, message, and close.
    */
  def on(`type`: String, callback: js.Function0[Unit]): Nothing = js.native

  def send(data: js.Object, options: js.Array[String]): Nothing = js.native

  def emit(event: String, data: js.Object, options: js.Array[String]): Nothing = js.native
}
