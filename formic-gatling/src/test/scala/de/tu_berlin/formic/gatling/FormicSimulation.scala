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

  val dataTypeInstanceId = DataTypeInstanceId()

  val scn = scenario("FormicSimulation")
    .exec(formic("Creation")
      .create()
      .linear(dataTypeInstanceId))
    .pause(5)
      .repeat(10, "n") {
        exec(formic("LinearInsertion")
          .linear(dataTypeInstanceId)
          .insert('a')
          .index(0))
      }
    .pause(1)


  setUp(scn.inject(atOnceUsers(1))).protocols(formicConfig)

}
