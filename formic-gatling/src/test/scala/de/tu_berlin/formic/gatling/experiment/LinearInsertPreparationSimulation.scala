package de.tu_berlin.formic.gatling.experiment

import de.tu_berlin.formic.common.{ClientId, DataTypeInstanceId}
import de.tu_berlin.formic.gatling.Predef._
import io.gatling.core.Predef._

/**
  * @author Ronny Bräunlich
  */
class LinearInsertPreparationSimulation extends Simulation {

  val NUM_DATATYPES = 1

  val NUM_EDITORS = 20

  val formicConfig = formic
    .url("http://localhost:8080")
    .username(ClientId())
    .bufferSize(100)
    .logLevel("info")

  //to have a feeder for all scenarios, we create the ids up front and use them
  val dataTypeInstanceIdFeeder = for (x <- 0.until(NUM_DATATYPES)) yield Map("dataTypeInstanceId" -> DataTypeInstanceId().id)

  val connect = exec(formic("Connection").connect())
    .pause(2)

  val createWarmupDataType = feed(dataTypeInstanceIdFeeder.iterator) //IMPORTANT, use an iterator or both scenarios will share one, which results in Exceptions
    .exec(formic("Creation")
    .create()
    .linear("${dataTypeInstanceId}"))
    .pause(5)

  val createTestDataType =
    exec(s => s.set("formicId", DataTypeInstanceId().id))
      .exec(formic("DataType")
        .create()
        .linear("${formicId}"))
      .pause(1)
      .exec(s => {
        println("Id: " + s("formicId").as[String])
        s
      })
      .pause(1)

  val edit = repeat(200, "n") {
    exec(formic("LinearInsertion")
      .linear("${dataTypeInstanceId}")
      .insert('a')
      .index("${n}"))
  }.pause(10)
    .repeat(200, "n") {
      exec(formic("LinearInsertion")
        .linear("${dataTypeInstanceId}")
        .insert('a')
        .index("${n}"))
    }.pause(10)
    .repeat(101, "n") {
      exec(formic("LinearInsertion")
        .linear("${dataTypeInstanceId}")
        .insert('a')
        .index("${n}"))
    }.pause(10)


  val warmup = scenario("Warmup").exec(connect, createWarmupDataType, edit, createTestDataType)

  setUp(
    warmup.inject(atOnceUsers(NUM_DATATYPES))
  ).protocols(formicConfig)

}

