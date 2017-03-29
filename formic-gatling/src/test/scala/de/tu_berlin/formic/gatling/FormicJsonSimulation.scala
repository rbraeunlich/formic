package de.tu_berlin.formic.gatling

import de.tu_berlin.formic.common.{ClientId, DataStructureInstanceId}
import de.tu_berlin.formic.gatling.Predef._
import de.tu_berlin.formic.gatling.action.{SessionVariables, TimeMeasureCallback}
import io.gatling.core.Predef._

/**
  * @author Ronny Bräunlich
  */
class FormicJsonSimulation extends Simulation {

  val formicConfig = formic
    .url("http://localhost:8080")
    .bufferSize(100)
    .logLevel("info")

  val prefixes = ('a' to 'z').iterator

  //to have a feeder for all scenarios, we create the ids up front and use them
  val dataStructureInstanceIdFeeder = for (x <- 0.until(3)) yield Map("dataStructureInstanceId" -> DataStructureInstanceId().id)

  val connect = exec(formic("Connection").connect())
    .pause(2)

  val createDataTypes = feed(dataStructureInstanceIdFeeder.iterator) //IMPORTANT, use an iterator or both scenarios will share one, which results in Exceptions
    .exec(formic("Creation")
    .create()
    .json("${dataStructureInstanceId}"))
    .pause(5)

  val edit =
    exec(_.set("prefix", prefixes.next().toString))
      .repeat(10, "n") {
        exec(formic("JsonInsertion")
          .json("${dataStructureInstanceId}")
          .insert("test")
          .path(Seq("${prefix}${n}")))
      }
      .pause(1)
      .repeat(5, "n") {
        exec(formic("JsonReplacement")
          .json("${dataStructureInstanceId}")
          .replace("text")
          .path(Seq("${prefix}${n}")))
      }
      .repeat(4, "n") {
        exec(formic("JsonDeletion")
          .json("${dataStructureInstanceId}")
          .remove(Seq("${prefix}${n}")))
      }
      .pause(30)
      .exec(s =>{
        s(SessionVariables.TIMEMEASURE_CALLBACK).as[TimeMeasureCallback].cancelAll()
        s
      })

  val subscribe = feed(dataStructureInstanceIdFeeder.iterator.toArray.random)
    .exec(formic("Subscription")
      .subscribe("${dataStructureInstanceId}"))
    .pause(1)

  val creators = scenario("Creators").exec(connect, createDataTypes)

  val editors = scenario("Editors")
    .exec(connect).pause(7).exec(subscribe, edit)

  setUp(
    creators.inject(atOnceUsers(3)),
    editors.inject(rampUsers(10) over 5)
  ).protocols(formicConfig)

}
