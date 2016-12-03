package de.tu_berlin.formic.example

import de.tu_berlin.formic.client.{FormicSystem, NewInstanceCallback}
import de.tu_berlin.formic.common.ClientId
import de.tu_berlin.formic.common.datatype.{DataTypeName, FormicDataType}

import scala.scalajs.js

/**
  * @author Ronny Bräunlich
  */
object Main extends js.JSApp {

  override def main(): Unit = {
    val system = new FormicSystem()
    system.init(new ExampleCallback(), ClientId("Test-User"))
  }

}