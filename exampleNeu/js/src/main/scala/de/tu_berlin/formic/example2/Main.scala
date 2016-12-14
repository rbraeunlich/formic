package de.tu_berlin.formic.example2

import com.typesafe.config.ConfigFactory
import de.tu_berlin.formic.client.FormicSystem
import de.tu_berlin.formic.common.{ClientId, DataTypeInstanceId}
import de.tu_berlin.formic.datatype.linear.client.FormicString
import org.scalajs.dom.document
import org.scalajs.dom.raw.HTMLInputElement

import scala.collection.mutable.ArrayBuffer
import scala.scalajs.js.JSApp
import scala.util.{Failure, Success}
import org.scalajs.jquery.{JQueryEventObject, jQuery}

/**
  * @author Ronny Bräunlich
  */
object Main extends JSApp {

  val system = new FormicSystem(ConfigFactory.load())

  implicit val ec = system.system.dispatcher

  val strings: ArrayBuffer[FormicString] = collection.mutable.ArrayBuffer()

  val BACKSPACE_CODE = 8

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
    val string = new FormicString(() => updateUI(id.id), system, id)
    strings += string
    val inputId = id.id
    jQuery("body").append("<p>String data type with id " + inputId + "</p>")
    jQuery("body").append("<textarea rows=\"30\" cols=\"50\" id=\"" + inputId + "\"></textarea>")
    jQuery("#" + inputId).keypress(keyPressHandler(inputId))
  }

  def updateUI(id: String): Unit = {
    strings.find(s => s.dataTypeInstanceId.id == id).get.getAll.foreach {
      buff => jQuery("#" + id).value(buff.mkString)
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
      if (event.which != BACKSPACE_CODE) {
        val character = event.which.toChar
        println("Inserting new Character: " + character)
        strings.find(s => s.dataTypeInstanceId.id == elementId).get.add(index, character)
      } else {
        //since a delete with backspace starts behind the character to delete
        strings.find(s => s.dataTypeInstanceId.id == elementId).get.remove(index - 1)
      }
    }
  }

  def updateUIForFormicString(string: FormicString): () => Unit = () => {
    string.getAll.onComplete {
      case Success(buff) => jQuery("#" + string.dataTypeInstanceId.id).value(buff.mkString)
      case Failure(ex) => throw ex
    }
  }
}