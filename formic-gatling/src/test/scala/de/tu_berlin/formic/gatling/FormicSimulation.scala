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
    .exec(formic("Creation").create().linear(DataTypeInstanceId())).pause(1)

  setUp(scn.inject(atOnceUsers(1))).protocols(formicConfig)

}
