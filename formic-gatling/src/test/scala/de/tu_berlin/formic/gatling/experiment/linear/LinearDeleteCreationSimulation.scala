package de.tu_berlin.formic.gatling.experiment.linear

import de.tu_berlin.formic.common.DataStructureInstanceId
import de.tu_berlin.formic.gatling.Predef._
import io.gatling.core.Predef._

/**
  * @author Ronny Bräunlich
  */
class LinearDeleteCreationSimulation extends Simulation {

  val NUM_DATATYPES = 4

  val formicConfig = formic
    .url(System.getProperty("formicServer"))
    .bufferSize(100)
    .logLevel("info")

  //to have a feeder for all scenarios, we create the ids up front and use them
  val dataStructureInstanceIdFeeder = for (x <- 0.until(NUM_DATATYPES)) yield Map("dataStructureInstanceId" -> DataStructureInstanceId().id)

  val connect = exec(formic("Connection").connect())
    .pause(2)


  val createTestDataType =
    feed(dataStructureInstanceIdFeeder.iterator)
      .exec(formic("DataType")
        .create()
        .linear("${dataStructureInstanceId}"))
      .pause(1)
      .repeat(110, "n") {
        exec(formic("LinearInsertion")
          .linear("${dataStructureInstanceId}")
          .insert('a')
          .index(0))
      }.pause(10)
      .pause(1)
      .exec(s => {
        println("Id: " + s("dataStructureInstanceId").as[String])
        s
      })


  val warmup = scenario("Warmup").exec(connect, createTestDataType)

  setUp(
    warmup.inject(atOnceUsers(NUM_DATATYPES))
  ).protocols(formicConfig)

}

