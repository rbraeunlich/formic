package de.tu_berlin.formic.gatling

import io.gatling.core.Predef._
import de.tu_berlin.formic.gatling.Predef._

/**
  * @author Ronny Bräunlich
  */
class FormicSimulation extends Simulation {

  val scn = scenario("FormicSimulation")
    .exec(formic())

}
