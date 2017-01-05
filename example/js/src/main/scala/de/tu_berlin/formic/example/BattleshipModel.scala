package de.tu_berlin.formic.example

import scala.util.Random

class BattleshipModel(val view: View) {

  val boardSize = 7
  val numShips = 3
  val shipLength = 3
  var shipsSunk = 0
  var ships: List[Ship] = List.fill(numShips)(null)
  val random = new Random()

  def generateShipLocations(): Unit = {
    var tempLocation: List[(Int, Int)] = List.empty
    for (x <- 0 until numShips) {
      do {
        tempLocation = generateShip().toList
      } while (isCollision(tempLocation))
      ships = ships.updated(x, Ship(tempLocation, List.fill(shipLength)(false)))
    }
    println(ships)
  }

  def generateShip() = {
    val direction = Math.random()

    if (direction < 0.5) {
      // horizontal
      val row = Random.nextInt(boardSize)
      val col = Random.nextInt(this.boardSize - this.shipLength)
      for (i <- 0 until shipLength) yield row -> (col + i)
    } else {
      // vertical
      val row = Random.nextInt(this.boardSize - this.shipLength)
      val col = Random.nextInt(boardSize)
      for (i <- 0 until shipLength) yield (row + i) -> col
    }
  }

  def isCollision(tempLocation: List[(Int, Int)]): Boolean = {
    for (i <- 0 until numShips) {
      val ship = ships(i)
      for (j <- tempLocation.indices) {
        if (ship != null && ship.location.contains(tempLocation(j))) return true
      }
    }
    false
  }

  def fire(coordinates: (Int, Int)): Boolean = {
    val shipHit = ships.find(s => s.location.contains(coordinates))
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
          ships = ships.updated(ships.indexOf(ship), hitShip)
          view.displayHit(coordinates._1 + "" + coordinates._2)
          view.displayMessage("HIT!")
          if (hitShip.isSunk) {
            view.displayMessage("You sunk my battleship")
            shipsSunk += 1
          }
          true
        }
    }
  }
}

case class Ship(location: List[(Int, Int)], hits: List[Boolean]) {
  def isSunk: Boolean = !hits.contains(false)
}
