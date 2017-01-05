package de.tu_berlin.formic.example

import de.tu_berlin.formic.datatype.json.{FormicJsonObject, JsonPath}
import org.scalajs.jquery.{JQueryEventObject, _}

import scala.concurrent.ExecutionContext
import scala.scalajs.js.Dynamic.global
import scala.scalajs.js.annotation.JSExport
import scala.util.Try

/**
  * @author Ronny Bräunlich
  */
@JSExport
class Battleship(val jsonObject: FormicJsonObject)(implicit val ec: ExecutionContext) {

  val fireButtonId = "#fireButton"

  val guessInputId = "#guessInput"

  val view = new View

  val model = new BattleshipModel(view, jsonObject)

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

class BattleshipController(val model: BattleshipModel, val view: View)(implicit val ec: ExecutionContext) {

  val alphabet = List('A', 'B', 'C', 'D', 'E', 'F', 'G')

  var guesses = 0

  def processGuess(guess: String) = {
    val location = parseGuess(guess)
    location match {
      case None =>
      case Some(value) =>
        guesses += 1
        val result = for {
          hit <- model.fire(value)
          numShips <- model.jsonObject.getValueAt[Int](JsonPath("numShips"))
          sunk <- model.jsonObject.getValueAt[Int](JsonPath("shipsSunk"))
        } yield (hit, numShips, sunk)
        result.map(values => {
          if (values._1 && values._2 == values._3) {
            view.displayMessage("You sank all my battleships, in " + this.guesses + " guesses")
          }
        })
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
          //FIXME replace the magic 7 here
        else if (row < 0 || row >= 7 || columnTry.get < 0 || columnTry.get >= 7) {
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
