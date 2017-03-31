package de.tu_berlin.formic.example

import de.tu_berlin.formic.datatype.json.{JsonPath, ObjectNode}
import org.scalajs.jquery.{JQueryEventObject, _}
import upickle.default._
import de.tu_berlin.formic.datatype.json.JsonFormicJsonDataStructureProtocol._
import de.tu_berlin.formic.datatype.json.client.FormicJsonObject

import scala.concurrent.ExecutionContext
import scala.scalajs.js.Dynamic.global
import scala.scalajs.js.annotation.JSExport
import scala.util.Try

/**
  * @author Ronny Bräunlich
  */
@JSExport
class Battleship()(implicit val ec: ExecutionContext) {

  val fireButtonId = "#fireButton"

  val guessInputId = "#guessInput"

  val view = new View

  var model: BattleshipModel = _

  var controller: BattleshipController = _

  private var initialised = false

  /**
    * This method is a workaround to use the callback interface for initialization
    * but to be initialized only once.
    */
  def invoke(jsonObject: FormicJsonObject) = {
    if (!initialised) {
      init(jsonObject, withNewModel = true)
    } else {
      controller.updateUI(jsonObject)
    }
  }

  @JSExport
  def init(jsonObject: FormicJsonObject, withNewModel: Boolean): Unit = {
    initialised = true
    model = new BattleshipModel(view, jsonObject, withNewModel)
    controller = new BattleshipController(model, view)
    val fireButton = jQuery(fireButtonId)
    fireButton.click(handleFireButton _)
    val guessInput = jQuery(guessInputId)
    guessInput.keypress(handleInputKeypress())
    if(withNewModel) model.generateShipLocations()
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

  /**
    * Update the UI by setting the appropriate CSS class to the div representing the game area.
    * Luckily is setting a CSS class idempotent and there is no need to check anything.
    */
  def updateUI(jsonObject: FormicJsonObject) = {
    println("Update UI")
    val shipsAndWater = for {
      ships <- jsonObject.getValueAt[List[ObjectNode]](JsonPath("ships"))
      water <- jsonObject.getValueAt[List[ObjectNode]](JsonPath("water"))
    } yield (ships, water)
    shipsAndWater.foreach {
      shipsAndWater =>
        val shipObjects: List[Ship] = shipsAndWater._1.map(node => write[ObjectNode](node)).map(json => read[Ship](json))
        val waterObjects = shipsAndWater._2.map(node => write[ObjectNode](node)).map(json => read[Water](json))
        println("SHips " + shipObjects)
        println("water " + waterObjects)
        shipObjects.flatMap(s => s.location.zip(s.hits)).filter(t => t._2).map(t => t._1).foreach(view.displayHit)
        waterObjects.filter(w => w.hit).map(w => w.coordinate).foreach(view.displayMiss)
    }
  }
}

class View {

  val messageAreaId = "#messageArea"

  def displayMessage(s: String) = {
    val messageArea = jQuery(messageAreaId)
    messageArea.empty()
    messageArea.text(s)
  }

  def displayHit(coordinate: (Int, Int)) = {
    val id = coordinate._1 + "" + coordinate._2
    val cell = jQuery("#" + id)
    cell.attr("class", "hit")
  }

  def displayMiss(coordinate: (Int, Int)) = {
    val id = coordinate._1 + "" + coordinate._2
    val cell = jQuery("#" + id)
    cell.attr("class", "miss")
  }
}
