package de.tu_berlin.formic.example

class BattleshipModel(val view: View) {

  val boardSize = 7
  val numShips = 3
  val shipLength = 3
  var shipsSunk = 0
  var ships: List[Ship] = List.fill(numShips)(null)

  def generateShipLocations(): Unit = {
    var tempLocation: List[Int] = List.empty
    for (x <- 0 until numShips) {
      do {
        tempLocation = generateShip()
      } while (isCollision(tempLocation))
      ships = ships.updated(x, Ship(tempLocation, List.fill(shipLength)(false)))
    }
    println(ships)
  }

  def generateShip(): List[Int] = {
    val direction = Math.random()
    var row = 0.0
    var col = 0.0

    if (direction < 0.5) {
      // horizontal
      row = Math.floor(Math.random() * this.boardSize)
      col = Math.floor(Math.random() * (this.boardSize - this.shipLength + 1))
    } else {
      // vertical
      row = Math.floor(Math.random() * (this.boardSize - this.shipLength + 1))
      col = Math.floor(Math.random() * this.boardSize)
    }

    var newLocations: List[Int] = List.empty

    for (i <- 0 until shipLength) {
      if (direction == 1) {
        newLocations = newLocations :+ (row + (col + i)).toInt
      } else {
        newLocations = newLocations :+ ((row + i) + col).toInt
      }
    }
    newLocations
  }

  def isCollision(tempLocation: List[Int]): Boolean = {
    for (i <- 0 until numShips) {
      val ship = ships(i)
      for (j <- tempLocation.indices) {
        if(ship != null && ship.location.contains(tempLocation(j))) return true
      }
    }
    false
  }

  def fire(coordinates: (Int, Int)): Boolean = {
    val linearCoordinate = coordinates._1 + coordinates._2
    val shipHit = ships.find(s => s.location.contains(linearCoordinate))
    shipHit match {
      case None =>
        view.displayMiss(coordinates._1 + "" + coordinates._2)
        view.displayMessage("You missed")
        false
      case Some(ship) =>
        val index = ship.location.indexOf(linearCoordinate)
        if (ship.hits(index)) {
          view.displayMessage("Oops, you already hit that location")
          true
        } else {
          val hitShip = Ship(ship.location, ship.hits.updated(index, true))
          ships = ships.updated(index, hitShip)
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

case class Ship(location: List[Int], hits: List[Boolean]) {
  def isSunk: Boolean = !hits.contains(false)
}
