package de.tu_berlin.formic.gatling

import de.tu_berlin.formic.common.{ClientId, DataTypeInstanceId}
import de.tu_berlin.formic.gatling.Predef._
import io.gatling.core.Predef._

/**
  * @author Ronny Bräunlich
  */
class FormicSimulation extends Simulation {

  val formicConfig = formic
    .url("http://localhost:8080")
    .username(ClientId())
    .bufferSize(100)
    .logLevel("info")

  val scn = scenario("FormicSimulation")
    .exec(formic("Connection").connect())
    .exec(formic("Creation")
      .create()
      .linear())
    .pause(5)
      .repeat(10, "n") {
        exec(formic("LinearInsertion")
          .linear()
          .insert('a')
          .index("${n}"))
      }
    .pause(1)
    .exec(formic("LinearDeletion")
      .linear()
      .remove(0))
    .pause(1)


  setUp(scn.inject(atOnceUsers(2))).protocols(formicConfig)

}
