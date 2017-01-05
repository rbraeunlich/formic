package de.tu_berlin.formic.example

import org.scalajs.jquery.{JQueryEventObject, _}

import scala.scalajs.js.Dynamic.global
import scala.scalajs.js.annotation.{JSExport, JSExportAll}
import scala.util.Try

/**
  * @author Ronny Bräunlich
  */
@JSExport
class Battleship {

  val fireButtonId = "#fireButton"

  val guessInputId = "#guessInput"

  val view = new View

  val model = new BattleshipModel(view)

  val controller = new BattleshipController(model, view)

  @JSExport
  def init(): Unit = {
    val fireButton = jQuery(fireButtonId)
    fireButton.click(handleFireButton _)
    //return keypress
    val guessInput = jQuery(guessInputId)
    guessInput.keypress(handleInputKeypress())
    model.generateShipLocations()
  }

  def handleFireButton() = {
    val guessInput = jQuery(guessInputId)
    val guess = guessInput.value().toString.toUpperCase
    controller.processGuess(guess)
    guessInput.value("")
  }

  def handleInputKeypress(): (JQueryEventObject) => Boolean = {
    event: JQueryEventObject => {
      if (event.which == 13) {
        jQuery(fireButtonId).trigger("click")
        false
      }
      else true
    }
  }
}

class BattleshipController(val model: BattleshipModel, val view: View) {

  val alphabet = List('A', 'B', 'C', 'D', 'E', 'F', 'G')

  var guesses = 0

  def processGuess(guess: String) = {
    val location = parseGuess(guess)
    location match {
      case None =>
      case Some(value) =>
        guesses += 1
        val hit = model.fire(value)
        if (hit && model.shipsSunk == model.numShips) {
          view.displayMessage("You sank all my battleships, in " + this.guesses + " guesses")
        }
    }
  }

  def parseGuess(guess: String): Option[(Int, Int)] = {
    if (guess == null || guess.length != 2) {
      global.alert("Oops, please enter a letter and a number on the board.")
    } else {
      val firstChar = guess.charAt(0)
      if (!alphabet.contains(firstChar)) global.alert("Oops, the first coordinate isn't on the board.")
      else {
        val row = alphabet.indexOf(firstChar)
        val columnTry = Try(Integer.valueOf(guess.charAt(1).toString))
        if (columnTry.isFailure) global.alert("Oops, the second coordinate isn't on the board.")
        else if (row < 0 || row >= model.boardSize || columnTry.get < 0 || columnTry.get >= model.boardSize) {
          global.alert("Oops, that's off the board!")
        } else {
          return Some(row, columnTry.get.toInt)
        }
      }
    }
    Option.empty
  }
}

class View {

  val messageAreaId = "#messageArea"

  def displayMessage(s: String) = {
    val messageArea = jQuery(messageAreaId)
    messageArea.empty()
    messageArea.text(s)
  }

  def displayHit(id: String) = {
    val cell = jQuery("#" + id)
    cell.attr("class", "hit")
  }

  def displayMiss(id: String) = {
    val cell = jQuery("#" + id)
    cell.attr("class", "miss")
  }
}
