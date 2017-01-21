package de.tu_berlin.formic.gatling

import de.tu_berlin.formic.common.{ClientId, DataTypeInstanceId}
import de.tu_berlin.formic.datatype.linear.client.FormicString
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

  //TODO make this a list and transform it to feeder
  var dataTypeInstanceId: DataTypeInstanceId = _

  val scn = scenario("FormicSimulation")
    .exec(formic("Connection").connect())
    .exec(formic("Creation")
      .create()
      .linear())
    .pause(5)
    //hack to transfer the id to the next scenario
    .exec(session => {
    dataTypeInstanceId = session("linear").as[FormicString].dataTypeInstanceId
    session
  })
    .repeat(10, "n") {
      exec(formic("LinearInsertion")
        .linear()
        .insert('a')
        .index("${n}"))
    }
    .pause(1)
    .exec(formic("LinearDeletion")
      .linear()
      .remove(0))
    .pause(1)

  val subscription = scenario("Subscription")
    .pause(5)
    .exec(formic("Connection").connect())
    .pause(1)
    .exec(_.set("dataTypeInstanceId", dataTypeInstanceId.id))
    .exec(formic("Subscription")
      .linear()
      .subscribe("${dataTypeInstanceId}"))
    .pause(1)

  setUp(
    scn.inject(atOnceUsers(1)),
    subscription.inject(atOnceUsers(1))
  ).protocols(formicConfig)

}
