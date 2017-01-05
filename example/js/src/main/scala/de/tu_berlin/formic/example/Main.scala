package de.tu_berlin.formic.example

import com.typesafe.config.ConfigFactory
import de.tu_berlin.formic.client.FormicSystem
import de.tu_berlin.formic.common.{ClientId, DataTypeInstanceId}
import de.tu_berlin.formic.datatype.json.FormicJsonObject
import de.tu_berlin.formic.datatype.linear.client.FormicString
import de.tu_berlin.formic.datatype.tree.{AccessPath, FormicIntegerTree}
import org.scalajs.dom.document
import org.scalajs.dom.raw.HTMLInputElement
import org.scalajs.jquery.{JQueryEventObject, jQuery}

import scala.collection.mutable.ArrayBuffer
import scala.scalajs.js.JSApp
import scala.util.{Failure, Success}

/**
  * @author Ronny Bräunlich
  */

class Main {
  val system = new FormicSystem(ConfigFactory.load())

  implicit val ec = system.system.dispatcher

  val strings: ArrayBuffer[FormicString] = collection.mutable.ArrayBuffer()

  val trees: ArrayBuffer[FormicIntegerTree] = collection.mutable.ArrayBuffer()

  val userId = ClientId()

  def start() = {
    system.init(new ExampleCallback(this), userId)
    jQuery(setupUI _)
  }

  def setupUI() = {
    jQuery("#userId").text(s"User: ${userId.id}")
    jQuery("#subscribe-button").click(subscribe _)
    jQuery("#new-string-button").click(createNewString _)
    jQuery("#new-tree-button").click(createNewTree _)
    jQuery("#startButton").click(startBattleship _)
  }

  def createNewString() = {
    val id = DataTypeInstanceId()
    val string = new FormicString(updateUIForString(id), system, id)
    strings += string
    val inputId = id.id
    jQuery("body").append("<p class=\"stringId\">String data type with id " + inputId + "</p>")
    jQuery("body").append("<textarea rows=\"30\" cols=\"50\" class=\"stringInput\" id=\"" + inputId + "\"></textarea>")
    jQuery("#" + inputId).keypress(keyPressHandler(inputId))
  }

  def createNewTree() = {
    val id = DataTypeInstanceId()
    val tree = new FormicIntegerTree(updateUIForTree(id), system, id)
    trees += tree
    insertBasicTreeElements(id.id)
  }

  def updateUIForString(id: DataTypeInstanceId): () => Unit = () => {
    strings.find(s => s.dataTypeInstanceId == id).get.getAll.foreach {
      buff =>
        val textInput = jQuery("#" + id.id)
        textInput.value(buff.mkString)
    }
  }

  def updateUIForTree(id: DataTypeInstanceId): () => Unit = () => {
    trees.find(s => s.dataTypeInstanceId == id).get.getTree().onComplete {
      case Success(rootNode) =>
        val treeDiv = jQuery("#" + id.id)
        treeDiv.empty()
        treeDiv.append(s"""Tree data type with id ${id.id}""")
        val ui = new UITree(id.id, rootNode)
        ui.drawTree()
      case Failure(ex) => throw ex
    }
  }

  def subscribe(): Unit = {
    val id = jQuery("#subscribe-id").value()
    system.requestDataType(DataTypeInstanceId(id.toString))
  }

  //common functions needed by Main and the callback
  def keyPressHandler(elementId: String): (JQueryEventObject) => Boolean = {
    (event: JQueryEventObject) => {
      //this is quite some hack
      val index = document.getElementById(elementId).asInstanceOf[HTMLInputElement].selectionStart
      if (!Main.keysToIgnore.contains(event.which)) {
        val character = event.which.toChar
        println("Inserting new Character: " + character)
        strings.find(s => s.dataTypeInstanceId.id == elementId).get.add(index, character)
      } else {
        //since a delete with backspace starts behind the character to delete
        strings.find(s => s.dataTypeInstanceId.id == elementId).get.remove(index - 1)
      }
      false
    }
  }


  def insertValueToTree(id: String): (JQueryEventObject) => Unit = {
    (eventObject: JQueryEventObject) => {
      val tree = trees.find(s => s.dataTypeInstanceId.id == id).get
      val toInsert = jQuery("#input" + id).value()
      val where = jQuery("#path" + id).`val`().toString.split("/").filter(s => s.nonEmpty).map(s => s.toInt)
      tree.insert(toInsert.toString.toInt, AccessPath(where: _*))
    }
  }

  def deleteFromTree(id: String): (JQueryEventObject) => Unit = {
    (eventObject: JQueryEventObject) => {
      println("delete value from tree")
      val tree = trees.find(s => s.dataTypeInstanceId.id == id).get
      val where = jQuery("#path" + id).`val`().toString.split("/").filter(s => s.nonEmpty).map(s => s.toInt)
      tree.remove(AccessPath(where: _*))
    }
  }

  def insertBasicTreeElements(id: String) = {
    jQuery("body").append(s"""<div id=\"head$id\">""")
    val headElements = new StringBuilder
    headElements ++= "<p>"
    headElements ++= s"""<button id="insert$id">Insert</button>"""
    headElements ++= s"""<input id="input$id" type="number" step="1">"""
    headElements ++= """</br>"""
    headElements ++= s"""<button id="delete$id">Delete</button>"""
    headElements ++= "</p>"
    jQuery(s"#head$id").append(headElements.toString())
    jQuery("body").append("</div>")
    jQuery("body").append(s"""<div id=\"$id\">""")
    jQuery("body").append("</div>")
    jQuery("#insert" + id).click(insertValueToTree(id))
    jQuery("#delete" + id).click(deleteFromTree(id))
  }

  def startBattleship() = {
    var gameInput = jQuery("#gameInput").value().toString
    if (gameInput == null || gameInput.trim.isEmpty) {
      gameInput = DataTypeInstanceId().id
      jQuery("#gameInput").value(DataTypeInstanceId().id)
    }
    jQuery("#gameInput").prop("disabled", true)
    jQuery("#startButton").prop("disabled", true)
    jQuery("#guessInput").prop("disabled", false)
    jQuery("#fireButton").prop("disabled", false)
    val json = new FormicJsonObject(() => {}, system, DataTypeInstanceId(gameInput))
    new Battleship(json).init()
  }
}

object Main extends JSApp {

  val BACKSPACE_CODE = 8
  val ARROWKEY_DOWN = 40
  val ARROWKEY_UP = 38
  val ARROWKEY_LEFT = 37
  val ARROWKEY_RIGHT = 39

  val keysToIgnore = List(BACKSPACE_CODE, ARROWKEY_DOWN, ARROWKEY_UP, ARROWKEY_LEFT, ARROWKEY_RIGHT)

  def main(): Unit = {
    val mainClazz = new Main
    mainClazz.start()
  }
}