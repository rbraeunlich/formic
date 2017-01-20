package de.tu_berlin.formic.gatling

import de.tu_berlin.formic.common.DataTypeInstanceId
import de.tu_berlin.formic.gatling.Predef._
import io.gatling.core.Predef._

/**
  * @author Ronny Bräunlich
  */
class FormicSimulation extends Simulation {

  val formicConfig = formic
    .url("http://localhost:8080")
    .username("realShady")
    .bufferSize(100)
    .logLevel("info")

  val scn = scenario("FormicSimulation")
    .exec(formic("Creation").create().linear(DataTypeInstanceId()))

  setUp(scn.inject(atOnceUsers(1))).protocols(formicConfig)

}
