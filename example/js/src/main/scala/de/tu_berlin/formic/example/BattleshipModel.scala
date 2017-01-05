package de.tu_berlin.formic.example

import de.tu_berlin.formic.datatype.json._
import upickle.default._
import de.tu_berlin.formic.datatype.json.JsonFormicJsonDataTypeProtocol._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Random

class BattleshipModel(val view: View, val jsonObject: FormicJsonObject)(implicit val ec: ExecutionContext) {

  jsonObject.insert(7, JsonPath("boardSize"))
  jsonObject.insert(3, JsonPath("numShips"))
  jsonObject.insert(3, JsonPath("shipLength"))
  jsonObject.insert(0, JsonPath("shipsSunk"))

  val random = new Random()

  def generateShipLocations(): Unit = {
    var tempLocation: List[(Int, Int)] = List.empty
    val fields = for {
      sizeResult <- jsonObject.getValueAt[Int](JsonPath("boardSize"))
      numShipsResult <- jsonObject.getValueAt[Int](JsonPath("numShips"))
      shipLengthResult <- jsonObject.getValueAt[Int](JsonPath("shipLength"))
      shipsSunkResult <- jsonObject.getValueAt[Int](JsonPath("shipsSunk"))
    } yield (sizeResult, numShipsResult, shipLengthResult, shipsSunkResult)
    fields.foreach(results => {
      var temporalShips: List[Ship] = List.empty
      for (x <- 0 until results._2) {
        do {
          tempLocation = generateShip(results._1, results._2, results._3).toList
        } while (isCollision(tempLocation, results._2, temporalShips))
        temporalShips = temporalShips :+ Ship(tempLocation, List.fill(results._3)(false))
      }
      jsonObject.insertArray(temporalShips.toArray, JsonPath("ships"))
    })
    //jsonObject.getValueAt[List[ObjectNode]](JsonPath("ships")).foreach(s => println("Ship: " + s))
  }

  def generateShip(boardSize: Int, numShips: Int, shipLength: Int) = {
    val direction = Math.random()

    if (direction < 0.5) {
      // horizontal
      val row = Random.nextInt(boardSize)
      val col = Random.nextInt(boardSize - shipLength)
      for (i <- 0 until shipLength) yield row -> (col + i)
    } else {
      // vertical
      val row = Random.nextInt(boardSize - shipLength)
      val col = Random.nextInt(boardSize)
      for (i <- 0 until shipLength) yield (row + i) -> col
    }
  }

  def isCollision(tempLocation: List[(Int, Int)], numShips: Int, otherShips: List[Ship]): Boolean = {
    for (ship <- otherShips) {
      for (j <- tempLocation.indices) {
        if (ship != null && ship.location.contains(tempLocation(j))) return true
      }
    }
    false
  }

  def fire(coordinates: (Int, Int)): Future[Boolean] = {
    val result: Future[Boolean] = jsonObject.getValueAt[List[ObjectNode]](JsonPath("ships")).map(
      ships => {
        val shipObjects: List[Ship] = ships.map(node => write[ObjectNode](node.asInstanceOf[ObjectNode])).map(json => read[Ship](json))
        val shipHit = shipObjects.find(s => s.location.contains(coordinates))
        shipHit match {
          case None =>
            view.displayMiss(coordinates._1 + "" + coordinates._2)
            view.displayMessage("You missed")
            false
          case Some(ship) =>
            val index = ship.location.indexOf(coordinates)
            if (ship.hits(index)) {
              view.displayMessage("Oops, you already hit that location")
              true
            } else {
              val hitShip = Ship(ship.location, ship.hits.updated(index, true))
              jsonObject.replace(hitShip, JsonPath("ships", shipObjects.indexOf(ship).toString))
              view.displayHit(coordinates._1 + "" + coordinates._2)
              view.displayMessage("HIT!")
              if (hitShip.isSunk) {
                view.displayMessage("You sunk my battleship")
                //TODO if two people sink a ship in parallel this will be one too much
                jsonObject.getValueAt[Int](JsonPath("shipsSunk")).foreach(value => jsonObject.replace(value + 1, JsonPath("shipsSunk")))
              }
              true
            }
        }
      }
    )
    result
  }
}

case class Ship(location: List[(Int, Int)], hits: List[Boolean]) {
  def isSunk: Boolean = !hits.contains(false)
}
