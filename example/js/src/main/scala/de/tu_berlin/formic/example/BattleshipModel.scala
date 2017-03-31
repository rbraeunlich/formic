package de.tu_berlin.formic.example

import de.tu_berlin.formic.datatype.json.JsonFormicJsonDataStructureProtocol._
import de.tu_berlin.formic.datatype.json._
import de.tu_berlin.formic.datatype.json.client.FormicJsonObject
import upickle.default._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Random

class BattleshipModel(val view: View, val jsonObject: FormicJsonObject, withNewModel: Boolean)(implicit val ec: ExecutionContext) {

  if(withNewModel) {
    jsonObject.insert(7, JsonPath("boardSize"))
    jsonObject.insert(3, JsonPath("numShips"))
    jsonObject.insert(3, JsonPath("shipLength"))
    jsonObject.insert(0, JsonPath("shipsSunk"))
  }
  val random = new Random()

  def generateShipLocations(): Unit = {
    var tempLocation: List[(Int, Int)] = List.empty
    val fields = for {
      sizeResult <- jsonObject.getValueAt[Int](JsonPath("boardSize"))
      numShipsResult <- jsonObject.getValueAt[Int](JsonPath("numShips"))
      shipLengthResult <- jsonObject.getValueAt[Int](JsonPath("shipLength"))
    } yield (sizeResult, numShipsResult, shipLengthResult)
    fields.foreach(results => {
      val boardSize = results._1
      val numShips = results._2
      val shipLength = results._3
      var generatedShips: List[Ship] = List.empty
      for (x <- 0 until results._2) {
        do {
          tempLocation = generateShip(boardSize, numShips, shipLength).toList
        } while (isCollision(tempLocation, numShips, generatedShips))
        generatedShips = generatedShips :+ Ship(tempLocation, List.fill(shipLength)(false))
      }
      jsonObject.insert(generatedShips.toArray, JsonPath("ships"))
      val generatedWater = generateWater(generatedShips.flatMap(s => s.location).toSet, boardSize)
      jsonObject.insert(generatedWater.toArray, JsonPath("water"))
    })
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

  /**
    * To be able to display misses for other users we need water that remembers if someone
    * fired at it.
    *
    * @param shipLocations the locations of the ships, where we should not place water
    * @param boardSize     the size of the game board
    */
  def generateWater(shipLocations: Set[(Int, Int)], boardSize: Int): Set[Water] = {
    val allPossibleCoordinates = for (x <- 0.until(boardSize); y <- 0.until(boardSize)) yield (x, y)
    val freeCoordinates = allPossibleCoordinates.diff(shipLocations.toSeq)
    freeCoordinates.map(coord => Water(coord, hit = false)).toSet
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
    val shipsAndWater = for {
      ships <- jsonObject.getValueAt[List[ObjectNode]](JsonPath("ships"))
      water <- jsonObject.getValueAt[List[ObjectNode]](JsonPath("water"))
    } yield (ships, water)
    val result: Future[Boolean] = shipsAndWater.map(
      shipsAndWater => {
        val ships = shipsAndWater._1
        val water = shipsAndWater._2
        val shipObjects: List[Ship] = ships.map(node => write[ObjectNode](node)).map(json => read[Ship](json))
        val shipHit = shipObjects.find(s => s.location.contains(coordinates))
        shipHit match {
          case None =>
            val waterObjects = water.map(node => write[ObjectNode](node)).map(json => read[Water](json))
            val hitWater = waterObjects.zipWithIndex.find(t => t._1.coordinate == coordinates).get
            jsonObject.replace(Water(hitWater._1.coordinate, hit = true), JsonPath("water", hitWater._2.toString))
            //view.displayMiss(coordinates)
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
              //view.displayHit(coordinates)
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

case class Water(coordinate: (Int, Int), hit: Boolean)