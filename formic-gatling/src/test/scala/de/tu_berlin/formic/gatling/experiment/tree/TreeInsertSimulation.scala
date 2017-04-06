package de.tu_berlin.formic.gatling.experiment.tree

import de.tu_berlin.formic.datastructure.tree.TreeNode
import de.tu_berlin.formic.datastructure.tree.client.FormicIntegerTree
import de.tu_berlin.formic.gatling.Predef._
import de.tu_berlin.formic.gatling.action.{SessionVariables, TimeMeasureCallback}
import io.gatling.core.Predef._

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._

/**
  * @author Ronny Bräunlich
  */
class TreeInsertSimulation extends Simulation {

  //zero is the special value for only one user on localhost
  val SINGLE_USER = System.getProperty("formicEditors").toInt == 0

  val NUM_DATATYPES = 1

  val NUM_EDITORS = if(SINGLE_USER) 1 else System.getProperty("formicEditors").toInt

  val NUM_OPERATIONS = 100

  val NUM_EDITS = NUM_OPERATIONS / (NUM_EDITORS * (if(SINGLE_USER) 1 else 5)) //five workers

  val DATATYPEINSTANCEID = System.getProperty("formicId")

  val SERVER_ADDRESS = System.getProperty("formicServer")

  val WORKER_NR = System.getProperty("workerNumber").toInt

  val formicConfig = formic
    .url(SERVER_ADDRESS)
    .bufferSize(100)
    .logLevel("info")

  //to have a feeder for all scenarios, we create the ids up front and use them
  val dataTypeInstanceIdFeeder = Seq(Map("dataStructureInstanceId" -> DATATYPEINSTANCEID))

  val connect = exec(formic("Connection").connect())
    .pause(2)

  val edit = repeat(NUM_EDITS, "n") {
    exec(formic("LinearInsertion")
      .tree("${dataStructureInstanceId}")
      .insert(Integer.toString(WORKER_NR).toCharArray.head)
      .path(Seq("${n}")))
      .pause(1)
  }
    .pause(30)
    .exec(s => {
      s(SessionVariables.TIMEMEASURE_CALLBACK).as[TimeMeasureCallback].cancelAll()
      s
    })

  val check = rendezVous(NUM_EDITORS)
    .exec(s => {
      val formicTree = s(dataTypeInstanceIdFeeder.head("dataStructureInstanceId").get).as[FormicIntegerTree]
      TreeInsertSimulation.addTree(formicTree)
      s
    })
    .pause(1)
    .rendezVous(NUM_EDITORS)
    .exec(s => {
      //this executes the method several times but that's ok
      TreeInsertSimulation.checkTreesForConsistency()
      s
    })
    .pause(1)

  val subscribe = feed(dataTypeInstanceIdFeeder.iterator.toArray.circular)
    .exec(formic("Subscription")
      .subscribe("${dataStructureInstanceId}"))
    .pause(10)

  val editors = scenario("Editors")
    .exec(connect).pause(7).exec(subscribe, edit, check)

  setUp(
    editors.inject(atOnceUsers(NUM_EDITORS))
  ).protocols(formicConfig)

}

object TreeInsertSimulation {

  import scala.concurrent.ExecutionContext.Implicits.global

  var formicTrees = List.empty[FormicIntegerTree]

  def addTree(s: FormicIntegerTree) = {
    formicTrees = s :: formicTrees
  }

  def checkTreesForConsistency() = {
    //FIXME this check only works within the same JVM, not for distributed tests
    val futureStrings: List[Future[TreeNode]] = formicTrees.map(s => s.getTree())
    val future = Future.sequence(futureStrings)
    val result = Await.result(future, 20.seconds)
    result.combinations(2).foreach(comb => {
      if (comb(0) != comb(1)) {
        throw new AssertionError(s"Strings do not match: $comb")
      }
    })
    println("All equal: " + result.head)
  }
}

