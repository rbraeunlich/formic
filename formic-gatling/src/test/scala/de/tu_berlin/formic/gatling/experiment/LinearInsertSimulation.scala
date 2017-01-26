package de.tu_berlin.formic.gatling.experiment

import de.tu_berlin.formic.common.ClientId
import de.tu_berlin.formic.datatype.linear.client.FormicString
import de.tu_berlin.formic.gatling.Predef._
import de.tu_berlin.formic.gatling.action.{SessionVariables, TimeMeasureCallback}
import io.gatling.core.Predef._

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

/**
  * @author Ronny Bräunlich
  */
class LinearInsertSimulation extends Simulation {

  val NUM_DATATYPES = 1

  val NUM_EDITORS = 20

  val NUM_OPERATIONS = 100

  val NUM_EDITS = NUM_OPERATIONS / NUM_EDITORS

  val DATATYPEINSTANCEID = System.getProperty("formicId")

  val formicConfig = formic
    .url("http://localhost:8080")
    .username(ClientId())
    .bufferSize(100)
    .logLevel("info")

  //to have a feeder for all scenarios, we create the ids up front and use them
  val dataTypeInstanceIdFeeder = Seq(Map("dataTypeInstanceId" -> DATATYPEINSTANCEID))

  val connect = exec(formic("Connection").connect())
    .pause(2)

  val edit = repeat(NUM_EDITS, "n") {
    exec(formic("LinearInsertion")
      .linear("${dataTypeInstanceId}")
      .insert('a')
      .index("${n}"))
  }
    .pause(60)
    .exec(s => {
      s(SessionVariables.TIMEMEASURE_CALLBACK).as[TimeMeasureCallback].cancelAll()
      s
    })

  val check = rendezVous(NUM_EDITORS)
    .exec(s => {
      val formicString = s(dataTypeInstanceIdFeeder.head("dataTypeInstanceId").get).as[FormicString]
      LinearInsertSimulation.addString(formicString)
      s
    })
    .rendezVous(NUM_EDITORS)
    .exec(s => {
      //this executes the method several times but that's ok
      LinearInsertSimulation.checkAllStringsForConsistency()
      s
    })
    .rendezVous(NUM_EDITORS)

  val subscribe = feed(dataTypeInstanceIdFeeder.iterator.toArray.circular)
    .exec(formic("Subscription")
      .subscribe("${dataTypeInstanceId}"))
    .pause(10)

  val editors = scenario("Editors")
    .exec(connect).pause(7).exec(subscribe, edit, check)

  setUp(
    editors.inject(rampUsers(NUM_EDITORS) over 20)
  ).protocols(formicConfig)

}

object LinearInsertSimulation {

  import scala.concurrent.ExecutionContext.Implicits.global

  var formicStrings = List.empty[FormicString]

  def addString(s: FormicString) = {
    formicStrings = s :: formicStrings
  }

  def checkAllStringsForConsistency() = {
    //FIXME this check only works within the same JVM, not for distributed tests
    val futureStrings: List[Future[ArrayBuffer[Char]]] = formicStrings.map(s => s.getAll())
    val future = Future.sequence(futureStrings)
    val result = Await.result(future, 20.seconds).map(buff => buff.mkString)
    result.combinations(2).foreach(comb => {
      if (comb(0) != comb(1)) {
        throw new AssertionError(s"Strings do not match: $comb")
      }
    })
    println("All equal: " + result.head)
  }
}
