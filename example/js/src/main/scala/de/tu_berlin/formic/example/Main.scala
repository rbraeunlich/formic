package de.tu_berlin.formic.example

import com.typesafe.config.ConfigFactory
import de.tu_berlin.formic.client.FormicSystem
import de.tu_berlin.formic.common.{ClientId, DataTypeInstanceId}
import de.tu_berlin.formic.datatype.linear.client.FormicString
import org.scalajs.dom.document
import org.scalajs.dom.raw.HTMLInputElement
import org.scalajs.jquery.{JQueryEventObject, jQuery}

import scala.collection.mutable.ArrayBuffer
import scala.scalajs.js
import scala.scalajs.js.JSApp

/**
  * @author Ronny Bräunlich
  */
object Main extends JSApp {

  val system = new FormicSystem(ConfigFactory.load())

  implicit val ec = system.system.dispatcher

  val strings: ArrayBuffer[FormicString] = collection.mutable.ArrayBuffer()

  val BACKSPACE_CODE = 8
  val ARROWKEY_DOWN = 40
  val ARROWKEY_UP = 38
  val ARROWKEY_LEFT = 37
  val ARROWKEY_RIGHT = 39

  val keysToIgnore = List(BACKSPACE_CODE, ARROWKEY_DOWN, ARROWKEY_UP, ARROWKEY_LEFT, ARROWKEY_RIGHT)

  def main(): Unit = {
    system.init(new ExampleCallback(), ClientId())
    jQuery(setupUI _)
  }

  def setupUI() = {
    jQuery("#new-string-button").click(createNewString _)
    jQuery("#subscribe-button").click(subscribe _)
  }

  def createNewString() = {
    val id = DataTypeInstanceId()
    val string = new FormicString(updateUIForString(id), system, id)
    strings += string
    val inputId = id.id
    jQuery("body").append("<p>String data type with id " + inputId + "</p>")
    jQuery("body").append("<textarea rows=\"30\" cols=\"50\" id=\"" + inputId + "\"></textarea>")
    jQuery("#" + inputId).keypress(keyPressHandler(inputId))
  }

  def updateUIForString(id: DataTypeInstanceId): () => Unit = () => {
    strings.find(s => s.dataTypeInstanceId == id).get.getAll.foreach {
      buff =>
        val textInput = jQuery("#" + id.id)
        textInput.value(buff.mkString)
    }
  }

  def subscribe(): Unit = {
    val id = jQuery("#subscribe-id").value()
    system.requestDataType(DataTypeInstanceId(id.toString))
  }

  //common functions needed by Main and the callback
  def keyPressHandler(elementId: String): (JQueryEventObject) => Unit = {
    (event: JQueryEventObject) => {
      //this is quite some hack
      val index = document.getElementById(elementId).asInstanceOf[HTMLInputElement].selectionStart
      if (!keysToIgnore.contains(event.which)) {
        val character = event.which.toChar
        println("Inserting new Character: " + character)
        strings.find(s => s.dataTypeInstanceId.id == elementId).get.add(index, character)
      } else {
        //since a delete with backspace starts behind the character to delete
        strings.find(s => s.dataTypeInstanceId.id == elementId).get.remove(index - 1)
      }
    }
  }
}