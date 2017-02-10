package de.tu_berlin.formic.gatling.experiment.tree

import de.tu_berlin.formic.common.DataTypeInstanceId
import de.tu_berlin.formic.gatling.Predef._
import io.gatling.core.Predef._

/**
  * @author Ronny Bräunlich
  */
class TreeInsertCreationSimulation extends Simulation {

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
      .exec(formic("Insert root")
        .tree("${dataTypeInstanceId}")
        .insert(0)
        .path(Seq.empty))
      .exec(s => {
        println("Id: " + s("dataTypeInstanceId").as[String])
        s
      })
      .pause(1)


  val warmup = scenario("Warmup").exec(connect, createTestDataType)

  setUp(
    warmup.inject(atOnceUsers(NUM_DATATYPES))
  ).protocols(formicConfig)

}

