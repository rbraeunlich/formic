package de.tu_berlin.formic.gatling.experiment.json

import de.tu_berlin.formic.common.DataStructureInstanceId
import de.tu_berlin.formic.gatling.Predef._
import io.gatling.core.Predef._

/**
  * @author Ronny Bräunlich
  */
class JsonReplaceCreationSimulation extends Simulation {

  val NUM_DATATYPES = 4

  val formicConfig = formic
    .url(System.getProperty("formicServer"))
    .bufferSize(100)
    .logLevel("info")

  //to have a feeder for all scenarios, we create the ids up front and use them
  val dataStructureInstanceIdFeeder = for (x <- 0.until(NUM_DATATYPES)) yield Map("dataStructureInstanceId" -> DataStructureInstanceId().id)

  val connect = exec(formic("Connection").connect())
    .pause(2)

  val createWarmupDataType = feed(dataStructureInstanceIdFeeder.iterator) //IMPORTANT, use an iterator or both scenarios will share one, which results in Exceptions
    .exec(formic("Creation")
    .create()
    .json("${dataStructureInstanceId}"))
    .pause(5)

  val edit = repeat(101, "n") {
      exec(formic("jsonInsertion")
        .json("${dataStructureInstanceId}")
        .insert("foo")
        .path(Seq("${n}")))
    }.pause(10)
    .exec(s => {
      print(" " + s("dataStructureInstanceId").as[String])
      s
    })
    .pause(1)


  val warmup = scenario("Warmup").exec(connect, createWarmupDataType, edit)

  setUp(
    warmup.inject(atOnceUsers(NUM_DATATYPES))
  ).protocols(formicConfig)

}

