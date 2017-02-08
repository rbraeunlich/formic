package de.tu_berlin.formic.gatling

import de.tu_berlin.formic.common.{ClientId, DataTypeInstanceId}
import de.tu_berlin.formic.gatling.Predef._
import de.tu_berlin.formic.gatling.action.{SessionVariables, TimeMeasureCallback}
import io.gatling.core.Predef._

/**
  * @author Ronny Bräunlich
  */
class FormicTreeSimulation extends Simulation {

  val formicConfig = formic
    .url("http://localhost:8080")
    .bufferSize(100)
    .logLevel("info")

  //to have a feeder for all scenarios, we create the ids up front and use them
  val dataTypeInstanceIdFeeder = for (x <- 0.until(5)) yield Map("dataTypeInstanceId" -> DataTypeInstanceId().id)

  val connect = exec(formic("Connection").connect())
    .pause(2)

  val createDataTypes = feed(dataTypeInstanceIdFeeder.iterator) //IMPORTANT, use an iterator or both scenarios will share one, which results in Exceptions
    .exec(formic("Creation")
    .create()
    .tree("${dataTypeInstanceId}"))
    .pause(5)
    .exec(formic("Insert root")
      .tree("${dataTypeInstanceId}")
      .insert(0)
      .path(Seq.empty))

  val edit = repeat(10, "n") {
      exec(formic("LinearInsertion")
        .tree("${dataTypeInstanceId}")
        .insert(1)
        .path(Seq("${n}")))
    }
      .pause(1)
      .repeat(4, "n") {
        exec(formic("LinearDeletion")
          .tree("${dataTypeInstanceId}")
          .remove(Seq("${n}")))
      }
    .pause(15)
    .exec(s =>{
      s(SessionVariables.TIMEMEASURE_CALLBACK).as[TimeMeasureCallback].cancelAll()
      s
    })

  val subscribe = feed(dataTypeInstanceIdFeeder.iterator.toArray.random)
    .exec(formic("Subscription")
      .subscribe("${dataTypeInstanceId}"))
    .pause(1)

  val creators = scenario("Creators").exec(connect, createDataTypes)

  val editors = scenario("Editors")
    .exec(connect).pause(7).exec(subscribe, edit)

  setUp(
    creators.inject(atOnceUsers(5)),
    editors.inject(rampUsers(5) over 5)
  ).protocols(formicConfig)

}
