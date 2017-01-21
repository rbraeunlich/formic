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

  //to have a feeder for all scenarios, we create the ids up front and use them
  val dataTypeInstanceIdFeeder = for (x <- 0.until(1)) yield Map("dataTypeInstanceId" -> DataTypeInstanceId().id)

  println("Bananarama: " + dataTypeInstanceIdFeeder)

  val scn = scenario("FormicSimulation")
    .exec(formic("Connection").connect())
    .pause(1)
    .feed(dataTypeInstanceIdFeeder.iterator) //IMPORTANT, use an iterator or both scenarios will share one, which results in Exceptions
    .exec(formic("Creation")
      .create()
      .linear("${dataTypeInstanceId}"))
    .pause(5)
    .repeat(10, "n") {
      exec(formic("LinearInsertion")
        .linear("${dataTypeInstanceId}")
        .insert('a')
        .index("${n}"))
    }
    .pause(1)
    .exec(formic("LinearDeletion")
      .linear("${dataTypeInstanceId}")
      .remove(0))
    .pause(1)

  val subscription = scenario("Subscription")
    .pause(7)
    .exec(formic("Connection").connect())
    .pause(1)
    .feed(dataTypeInstanceIdFeeder.iterator)
    .exec(formic("Subscription")
      .linear("${dataTypeInstanceId}")
      .subscribe())
    .pause(1)

  setUp(
    scn.inject(atOnceUsers(1)),
    subscription.inject(atOnceUsers(1))
  ).protocols(formicConfig)

}
