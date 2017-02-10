package de.tu_berlin.formic.gatling.experiment.tree

import de.tu_berlin.formic.common.DataTypeInstanceId
import de.tu_berlin.formic.gatling.Predef._
import io.gatling.core.Predef._

/**
  * @author Ronny Bräunlich
  */
class TreeDeleteCreationSimulation extends Simulation {

  val NUM_DATATYPES = 4

  val formicConfig = formic
    .url(System.getProperty("formicServer"))
    .bufferSize(100)
    .logLevel("info")

  //to have a feeder for all scenarios, we create the ids up front and use them
  val dataTypeInstanceIdFeeder = for (x <- 0.until(NUM_DATATYPES)) yield Map("dataTypeInstanceId" -> DataTypeInstanceId().id)

  val connect = exec(formic("Connection").connect())
    .pause(2)


  val createTestDataType =
    feed(dataTypeInstanceIdFeeder.iterator)
      .exec(formic("DataType")
        .create()
        .tree("${dataTypeInstanceId}"))
      .pause(1)
      .exec(formic("Root insertion")
        .tree("${dataTypeInstanceId}")
        .insert(0)
        .path(Seq.empty))
      .repeat(110, "n") {
        exec(formic("LinearInsertion")
          .tree("${dataTypeInstanceId}")
          .insert(1)
          .path(Seq(0)))
      }.pause(10)
      .pause(1)
      .exec(s => {
        print(s("dataTypeInstanceId").as[String] + " ")
        s
      })


  val warmup = scenario("Warmup").exec(connect, createTestDataType)

  setUp(
    warmup.inject(atOnceUsers(NUM_DATATYPES))
  ).protocols(formicConfig)

}

