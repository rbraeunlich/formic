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

  val connect = exec(formic("Connection").connect())
    .pause(2)

  val createDataTypes = feed(dataTypeInstanceIdFeeder.iterator) //IMPORTANT, use an iterator or both scenarios will share one, which results in Exceptions
    .exec(formic("Creation")
    .create()
    .linear("${dataTypeInstanceId}"))
    .pause(5)

  val edit = repeat(10, "n") {
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

  val subscribe = feed(dataTypeInstanceIdFeeder.iterator)
    .exec(formic("Subscription")
      .linear("${dataTypeInstanceId}")
      .subscribe())
    .pause(1)

  val creators = scenario("Creators").exec(connect, createDataTypes)

  val editors = scenario("Editors").pause(7)
    .exec(connect, subscribe, edit)

  setUp(
    creators.inject(atOnceUsers(1)),
    editors.inject(atOnceUsers(1))
  ).protocols(formicConfig)

}
