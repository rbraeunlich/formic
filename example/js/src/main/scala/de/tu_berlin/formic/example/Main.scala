package de.tu_berlin.formic.example

import com.typesafe.config.ConfigFactory
import de.tu_berlin.formic.client.FormicSystemFactory
import de.tu_berlin.formic.common.datatype.client.{ClientDataStructureEvent, RemoteOperationEvent}
import de.tu_berlin.formic.common.{ClientId, DataStructureInstanceId}
import de.tu_berlin.formic.datatype.json.JsonPath
import de.tu_berlin.formic.datatype.json.client.FormicJsonObject
import de.tu_berlin.formic.datatype.linear.client.FormicString
import de.tu_berlin.formic.datatype.tree.AccessPath
import de.tu_berlin.formic.datatype.tree.client.FormicIntegerTree
import org.scalajs.dom.document
import org.scalajs.dom.raw.HTMLInputElement
import org.scalajs.jquery.{JQueryEventObject, jQuery}

import scala.collection.mutable.ArrayBuffer
import scala.scalajs.js.JSApp
import scala.util.{Failure, Success}

/**
  * @author Ronny Bräunlich
  */

class Main extends ExampleClientDataTypes {

  val config = ConfigFactory.parseString("akka {\n  loglevel = debug\n  http.client.idle-timeout = 10 minutes\n}\n\nformic {\n  server {\n    address = \"127.0.0.1\"\n    port = 8080\n  }\n  client {\n    buffersize = 100\n  }\n}")

  val system = FormicSystemFactory.create(config, dataTypeProvider)

  implicit val ec = system.system.dispatcher

  val strings: ArrayBuffer[FormicString] = collection.mutable.ArrayBuffer()

  val trees: ArrayBuffer[FormicIntegerTree] = collection.mutable.ArrayBuffer()

  val jsons: ArrayBuffer[FormicJsonObject] = collection.mutable.ArrayBuffer()

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
    jQuery("#new-json-button").click(createNewJson _)
    jQuery("#startButton").click(startBattleship _)
  }

  def createNewString() = {
    val id = DataStructureInstanceId()
    val string = new FormicString(updateUIForString(id), system, id)
    strings += string
    val inputId = id.id
    jQuery("body").append("<p class=\"stringId\">String data type with id " + inputId + "</p>")
    jQuery("body").append("<textarea rows=\"30\" cols=\"50\" class=\"stringInput\" id=\"" + inputId + "\"></textarea>")
    jQuery("#" + inputId).keypress(keyPressHandler(inputId))
  }

  def createNewTree() = {
    val id = DataStructureInstanceId()
    val tree = new FormicIntegerTree(updateUIForTree(id), system, id)
    trees += tree
    insertBasicTreeElements(id.id)
  }

  def createNewJson() = {
    val id = DataStructureInstanceId()
    val json = new FormicJsonObject(updateUIForJson(id), system, id)
    jsons += json
    insertJsonManipulationElements(id.id)
  }

  def updateUIForString(id: DataStructureInstanceId): (ClientDataStructureEvent) => Unit = {
    case RemoteOperationEvent(_) =>
      strings.find(s => s.dataStructureInstanceId == id).get.getAll.foreach {
        buff =>
          val textInput = jQuery("#" + id.id)
          textInput.value(buff.mkString)
      }
    case rest => //do nothing
  }

  def updateUIForTree(id: DataStructureInstanceId): (ClientDataStructureEvent) => Unit = (_) => {
    trees.find(s => s.dataStructureInstanceId == id).get.getTree().onComplete {
      case Success(rootNode) =>
        val treeDiv = jQuery("#" + id.id)
        treeDiv.empty()
        treeDiv.append(s"""Tree data type with id ${id.id}""")
        val ui = new UITree(id.id, rootNode)
        ui.drawTree()
      case Failure(ex) => throw ex
    }
  }

  def updateUIForJson(id: DataStructureInstanceId): (ClientDataStructureEvent) => Unit = (_) => {
    jsons.find(s => s.dataStructureInstanceId == id).get.getNodeAt(JsonPath()).onComplete {
      case Success(rootNode) =>
        val jsonDiv = jQuery("#" + id.id)
        jsonDiv.empty()
        jsonDiv.append(rootNode.toJsonString)
      case Failure(ex) => throw ex
    }
  }

  def subscribe(): Unit = {
    val id = jQuery("#subscribe-id").value()
    system.requestDataType(DataStructureInstanceId(id.toString))
  }

  //common functions needed by Main and the callback
  def keyPressHandler(elementId: String): (JQueryEventObject) => Unit = {
    (event: JQueryEventObject) => {
      //this is quite some hack
      val index = document.getElementById(elementId).asInstanceOf[HTMLInputElement].selectionStart
      if (!Main.keysToIgnore.contains(event.which)) {
        val character = event.which.toChar
        println("Inserting new Character: " + character)
        strings.find(s => s.dataStructureInstanceId.id == elementId).get.add(index, character)
      } else {
        //since a delete with backspace starts behind the character to delete
        strings.find(s => s.dataStructureInstanceId.id == elementId).get.remove(index - 1)
      }
    }
  }


  def insertValueToTree(id: String): (JQueryEventObject) => Unit = {
    (eventObject: JQueryEventObject) => {
      val tree = trees.find(s => s.dataStructureInstanceId.id == id).get
      val toInsert = jQuery("#input" + id).value()
      val where = jQuery("#path" + id).`val`().toString.split("/").filter(s => s.nonEmpty).map(s => s.toInt)
      tree.insert(toInsert.toString.toInt, AccessPath(where: _*))
    }
  }

  def deleteFromTree(id: String): (JQueryEventObject) => Unit = {
    (eventObject: JQueryEventObject) => {
      println("delete value from tree")
      val tree = trees.find(s => s.dataStructureInstanceId.id == id).get
      val where = jQuery("#path" + id).`val`().toString.split("/").filter(s => s.nonEmpty).map(s => s.toInt)
      tree.remove(AccessPath(where: _*))
    }
  }

  def insertJsonManipulationElements(id: String) = {
    jQuery("body").append(s"""<div id=\"head$id\">""")
    val headElements = new StringBuilder
    headElements ++= "<p>"
    headElements ++= s"""<button id="insertJson$id" class="insertButton" >Insert</button>"""
    headElements ++= s"""<input id="inputJson$id" class="inputJson" type="text">"""
    headElements ++= """</br>"""
    headElements ++= s"""<button id="deleteJson$id">Delete</button>"""
    headElements ++= """</br>"""
    headElements ++= s"""<button id="replaceJson$id">Replace</button>"""
    headElements ++= """</br>"""
    headElements ++= s"""Path<input id="pathJson$id" class="pathJson" type="text">"""
    headElements ++= "</p>"
    jQuery(s"#head$id").append(headElements.toString())
    jQuery("body").append("</div>")
    jQuery("body").append(s"""Json data type with id $id""")
    jQuery("body").append(s"""<div id="$id" class="jsonObject">""")
    jQuery("body").append("</div>")
    jQuery("#insertJson" + id).click(insertValueToJson(id))
    jQuery("#deleteJson" + id).click(deleteFromJson(id))
    jQuery("#replaceJson" + id).click(replaceInJson(id))
  }

  def insertValueToJson(id: String): (JQueryEventObject) => Unit = {
    (eventObject: JQueryEventObject) => {
      val json = jsons.find(s => s.dataStructureInstanceId.id == id).get
      val toInsert = jQuery("#inputJson" + id).value()
      val where = jQuery("#pathJson" + id).`val`().toString.split("/").filter(s => s.nonEmpty)
      json.insert(toInsert.toString, JsonPath(where: _*))
    }
  }

  def deleteFromJson(id: String): (JQueryEventObject) => Unit = {
    (eventObject: JQueryEventObject) => {
      val json = jsons.find(s => s.dataStructureInstanceId.id == id).get
      val where = jQuery("#pathJson" + id).`val`().toString.split("/").filter(s => s.nonEmpty)
      json.remove(JsonPath(where: _*))
    }
  }

  def replaceInJson(id: String): (JQueryEventObject) => Unit = {
    (eventObject: JQueryEventObject) => {
      val json = jsons.find(s => s.dataStructureInstanceId.id == id).get
      val toInsert = jQuery("#inputJson" + id).value()
      val where = jQuery("#pathJson" + id).`val`().toString.split("/").filter(s => s.nonEmpty)
      json.replace(toInsert.toString, JsonPath(where: _*))
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
    val gameInput = jQuery("#gameInput").value().toString
    if (gameInput == null || gameInput.trim.isEmpty) {
      val newId = DataStructureInstanceId()
      jQuery("#gameInput").value(newId.id)
      val battleship = new Battleship()
      val json: FormicJsonObject = new FormicJsonObject((_) => {
        battleship.invoke(jsons.find(j => j.dataStructureInstanceId == newId).get)
      }, system, newId)
      jsons += json
    } else {
      system.requestDataType(DataStructureInstanceId(gameInput))
    }
    jQuery("#gameInput").prop("disabled", true)
    jQuery("#startButton").prop("disabled", true)
    jQuery("#guessInput").prop("disabled", false)
    jQuery("#fireButton").prop("disabled", false)

  }

  def newCallbackForBattleship(jsonObject: FormicJsonObject): (ClientDataStructureEvent) => Unit = {
    val battleship = new Battleship()
    battleship.init(jsonObject, withNewModel = false)
    (_) => {
      battleship.invoke(jsonObject)
    }
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